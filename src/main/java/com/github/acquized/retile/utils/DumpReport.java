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
package com.github.acquized.retile.utils;

import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import com.eclipsesource.json.JsonObject;
import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.cache.impl.McAPICanada;
import com.github.acquized.retile.cache.impl.Offline;
import com.github.acquized.retile.sql.impl.MySQL;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class DumpReport {

    public static JsonObject create() throws IllegalAccessException, SQLException, IOException {
        JsonObject retilePlugin = new JsonObject().add("name", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDescription().getName())
                .add("version", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDescription().getVersion())
                .add("author", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDescription().getAuthor())
                .add("main", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDescription().getMain())
                .add("hash", Files.hash(ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDescription().getFile(), Hashing.md5()).toString());

        JsonObject server = new JsonObject().add("name", ProxyServer.getInstance().getName())
                .add("version", ProxyServer.getInstance().getVersion());
        JsonObject servers = new JsonObject();
        for(Map.Entry<String, ServerInfo> entry : ProxyServer.getInstance().getServers().entrySet()) {
            servers.add(entry.getKey(), entry.getValue().getAddress().toString());
        }
        server.add("servers", servers);

        JsonObject machine = new JsonObject().add("java", System.getProperty("java.version"))
                .add("system", System.getProperty("os.name"));

        JsonObject config = new JsonObject().add("prefix", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().prefix)
                .add("locale", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().locale)
                .add("forceOfflineUUID", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().forceOfflineUUID)
                .add("cooldown", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().cooldown)
                .add("clickableMsgs", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().clickableMsgs)
                .add("dateFormat", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().dateFormat)
                .add("version", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().version)
                .add("minPoolIdle", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().minPoolIdle)
                .add("maxPoolSize", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().maxPoolSize)
                .add("poolTimeout", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().poolTimeout)
                .add("reportAliases", Joiner.on(", ").join(ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().reportAliases))
                .add("reportsAliases", Joiner.on(", ").join(ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().reportsAliases))
                .add("toggleAliases", Joiner.on(", ").join(ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().toggleAliases))
                .add("infoAliases", Joiner.on(", ").join(ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().infoAliases))
                .add("queueAliases", Joiner.on(", ").join(ProjectRetile.getInjector().getInstance(ProjectRetile.class).getConfig().queueAliases));

        JsonObject dbConfig = new JsonObject().add("jdbcURL", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDbConfig().jdbcURL)
                .add("username", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDbConfig().username)
                .add("password", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDbConfig().password);

        JsonObject blacklist = new JsonObject().add("blacklist", Joiner.on(", ").join(ProjectRetile.getInjector().getInstance(ProjectRetile.class).getBlacklist().list.toArray()));

        JsonObject plugins = new JsonObject();
        ProxyServer.getInstance().getPluginManager().getPlugins().stream().filter(p -> !p.getDescription().getAuthor().equals("SpigotMC")).forEach(p -> {
            try {
                plugins.add(p.getDescription().getName(), new JsonObject()
                        .add("version", p.getDescription().getVersion())
                        .add("main", p.getDescription().getMain())
                        .add("author", p.getDescription().getAuthor())
                        .add("hash", Files.hash(p.getDescription().getFile(), Hashing.md5()).toString()));
            } catch (IOException ignored) {}
        });

        JsonObject database = new JsonObject().add("conntected", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDatabase().isConnected())
                .add("type", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDatabase() instanceof MySQL ? "MySQL" : "SQLite")
                .add("tableRetile", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDatabase().doesTableExist("retile"))
                .add("tableQueue", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDatabase().doesTableExist("queue"))
                .add("tableVersion", ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDatabase().doesTableExist("version"));

        JsonObject cache = new JsonObject();
        if(ProjectRetile.getInjector().getInstance(ProjectRetile.class).getCache() instanceof Offline) {
            cache.add("resolver", "BungeeCord");
            cache.add("values", "empty");
        } else {
            cache.add("resolver", "mcapi.ca");
            JsonObject values = new JsonObject();
            for(Map.Entry<UUID, String> entry : ((McAPICanada) ProjectRetile.getInjector().getInstance(ProjectRetile.class).getCache()).cache.asMap().entrySet()) {
                values.add(entry.getKey().toString(), entry.getValue());
            }
            cache.add("values", values);
        }

        return new JsonObject().add("retile", retilePlugin)
                .add("server", server)
                .add("machine", machine)
                .add("config", config)
                .add("dbConfig", dbConfig)
                .add("blacklist", blacklist)
                .add("plugins", plugins)
                .add("database", database)
                .add("cache", cache);
    }

}
