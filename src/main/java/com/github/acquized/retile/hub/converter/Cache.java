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
package com.github.acquized.retile.hub.converter;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.github.acquized.retile.ProjectRetile;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Cache {

    @Getter @Setter public static Cache instance;

    @Getter
    public final LoadingCache<UUID, String> cache = CacheBuilder.newBuilder()
            .maximumSize(3000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<UUID, String>() {
                @Override
                public String load(UUID key) throws Exception {
                    return resolve(key);
                }
            });

    @SneakyThrows
    public static String resolve(UUID uuid) {
        URL url = new URL("https://mcapi.ca/name/uuid/" + uuid.toString() + "?" + System.currentTimeMillis());
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.addRequestProperty("User-Agent", "ProjectRetile v" + ProjectRetile.getInstance().getDescription().getVersion());
        conn.setRequestMethod("GET");
        conn.setUseCaches(true);
        conn.setDoOutput(true);

        JsonObject obj = Json.parse(new InputStreamReader(conn.getInputStream())).asObject();
        return obj.get("name").asString();
    }

    @SneakyThrows
    public static UUID resolve(String name) {
        URL url = new URL("https://mcapi.ca/uuid/player/" + name + "?" + System.currentTimeMillis());
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.addRequestProperty("User-Agent", "ProjectRetile v" + ProjectRetile.getInstance().getDescription().getVersion());
        conn.setRequestMethod("GET");
        conn.setUseCaches(true);
        conn.setDoOutput(true);

        JsonObject obj = Json.parse(new InputStreamReader(conn.getInputStream())).asObject();
        return UUID.fromString(obj.get("uuid_formatted").asString());
    }

    @SneakyThrows
    public String username(UUID uuid) {
        return cache.get(uuid);
    }

    public UUID uuid(String name) {
        for(Map.Entry<UUID, String> entry : cache.asMap().entrySet()) {
            if(entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        return resolve(name);
    }

    @SneakyThrows
    public void addEntry(UUID uuid, String name) {
        if(cache.getIfPresent(uuid) == null) {
            cache.put(uuid, name);
        } else {
            removeEntry(uuid);
            cache.put(uuid, name);
        }
    }

    @SneakyThrows
    public void removeEntry(UUID uuid) {
        if(cache.getIfPresent(uuid) != null) {
            cache.invalidate(uuid);
        }
    }

}
