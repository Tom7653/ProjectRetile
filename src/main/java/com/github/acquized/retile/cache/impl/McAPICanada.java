/* Copyright 2016 Acquized
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.acquized.retile.cache.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.annotations.Beta;
import com.github.acquized.retile.cache.Cache;

import net.md_5.bungee.api.ProxyServer;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import lombok.Getter;

@Beta
public class McAPICanada implements Cache {

    @Getter
    public final LoadingCache<UUID, String> cache = CacheBuilder.newBuilder()
            .maximumSize(15000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<UUID, String>() {
                @Override
                public String load(UUID key) throws Exception {
                    return resolve(key).get();
                }
            });
    
    private ProjectRetile retile;
    
    @Inject
    public McAPICanada(ProjectRetile retile) {
        this.retile = retile;
    }

    public Future<String> resolve(UUID uuid) {
        FutureTask<String> task = new FutureTask<>(() -> {
            URL url = new URL("https://mcapi.ca/name/uuid/" + uuid.toString() + "?" + System.currentTimeMillis());
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.addRequestProperty("User-Agent", "ProjectRetile v" + retile.getDescription().getVersion());
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setDoOutput(true);

            JsonObject obj = Json.parse(new InputStreamReader(conn.getInputStream())).asObject();
            return obj.get("name").asString();
        });
        ProxyServer.getInstance().getScheduler().runAsync(retile, task);
        return task;
    }

    public Future<UUID> resolve(String name) {
        FutureTask<UUID> task = new FutureTask<>(() -> {
            URL url = new URL("https://mcapi.ca/uuid/player/" + name + "?" + System.currentTimeMillis());
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.addRequestProperty("User-Agent", "ProjectRetile v" + retile.getDescription().getVersion());
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setDoOutput(true);

            JsonObject obj = Json.parse(new InputStreamReader(conn.getInputStream())).asObject();
            addEntry(UUID.fromString(obj.get("uuid_formatted").asString()), name);
            return UUID.fromString(obj.get("uuid_formatted").asString());
        });
        ProxyServer.getInstance().getScheduler().runAsync(retile, task);
        return task;
    }

    @Override
    public String username(UUID uuid) {
        try {
            return cache.get(uuid);
        } catch (ExecutionException ex) {
            retile.getLog().error("Could not connect to local Cache regaring Username Resolving of '" + uuid.toString() + "'.", ex);
            return "Cache Failed @ " + uuid.hashCode();
        }
    }

    @Override
    public UUID uuid(String name) {
        for(Map.Entry<UUID, String> entry : cache.asMap().entrySet()) {
            if(entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        try {
            return resolve(name).get();
        } catch (InterruptedException | ExecutionException e) {
            return ProxyServer.getInstance().getPlayer(name).getUniqueId();
        }
    }

    @Override
    public void addEntry(UUID uuid, String name) {
        if(cache.getIfPresent(uuid) == null) {
            cache.put(uuid, name);
        } else {
            removeEntry(uuid);
            cache.put(uuid, name);
        }
    }

    @Override
    public void removeEntry(UUID uuid) {
        if(cache.getIfPresent(uuid) != null) {
            cache.invalidate(uuid);
        }
    }

}
