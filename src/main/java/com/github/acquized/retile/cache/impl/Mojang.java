/*
 * Copyright 2016 Acquized
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
import com.google.gson.JsonObject;

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
public class Mojang implements Cache {

    @Getter
    private final LoadingCache<UUID, String> cache = CacheBuilder.newBuilder()
            .maximumSize(Long.MAX_VALUE)
            .expireAfterWrite(3, TimeUnit.HOURS)
            .build(new CacheLoader<UUID, String>() {
                @Override
                public String load(UUID key) throws Exception {
                    return resolve(key).get();
                }
            });
    @Getter private int requests = 0;

    public Mojang() {
        ProxyServer.getInstance().getScheduler().schedule(ProjectRetile.getInstance(), () -> requests = 0, 10, 10, TimeUnit.MINUTES);
    }

    public Future<String> resolve(UUID uuid) {
        FutureTask<String> task = new FutureTask<>(() -> {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", ""));
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.addRequestProperty("User-Agent", "ProjectRetile v" + ProjectRetile.getInstance().getDescription().getVersion() + " (BungeeCord Server-Side Plugin)");
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setDoOutput(true);

            JsonObject obj = ProjectRetile.getInstance().getJsonParser().parse(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
            return obj.get("name").getAsString();
        });
        ProxyServer.getInstance().getScheduler().runAsync(ProjectRetile.getInstance(), task);
        return task;
    }

    public Future<UUID> resolve(String name) {
        FutureTask<UUID> task = new FutureTask<>(() -> {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.addRequestProperty("User-Agent", "ProjectRetile v" + ProjectRetile.getInstance().getDescription().getVersion() + " (BungeeCord Server-Side Plugin)");
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setDoOutput(true);

            JsonObject obj = ProjectRetile.getInstance().getJsonParser().parse(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
            return UUID.fromString(obj.get("id").getAsString().replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        });
        ProxyServer.getInstance().getScheduler().runAsync(ProjectRetile.getInstance(), task);
        requests++;
        return task;
    }

    @Override
    public String username(UUID uuid) {
        try {
            return cache.get(uuid);
        } catch (ExecutionException ex) {
            ProjectRetile.getInstance().getLog().error("Could not contact to local Cache regarding the username of " + uuid.toString() + ".", ex);
            return ProxyServer.getInstance().getPlayer(uuid).getName();
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
            if(requests >= 600) {
                ProjectRetile.getInstance().getLog().warn("ProjectRetile has reached Mojang's conversation service rate limit.");
                ProjectRetile.getInstance().getLog().warn("While were waiting for the limit to expire, we're using BungeeCord's UUID service.");
                ProjectRetile.getInstance().getLog().warn("We're using Mojang's conversation service again in 10 minutes.");
                return ProxyServer.getInstance().getPlayer(name).getUniqueId();
            } else {
                return resolve(name).get();
            }
        } catch (InterruptedException | ExecutionException ex) {
            ProjectRetile.getInstance().getLog().error("Could not resolve UUID of " + name + ".", ex);
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
