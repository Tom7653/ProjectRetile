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
        JsonObject retilePlugin = new JsonObject().add("name", ProjectRetile.getInstance().getDescription().getName())
                .add("version", ProjectRetile.getInstance().getDescription().getVersion())
                .add("author", ProjectRetile.getInstance().getDescription().getAuthor())
                .add("main", ProjectRetile.getInstance().getDescription().getMain())
                .add("hash", Files.hash(ProjectRetile.getInstance().getDescription().getFile(), Hashing.md5()).toString());

        JsonObject server = new JsonObject().add("name", ProxyServer.getInstance().getName())
                .add("version", ProxyServer.getInstance().getVersion());
        JsonObject servers = new JsonObject();
        for(Map.Entry<String, ServerInfo> entry : ProxyServer.getInstance().getServers().entrySet()) {
            servers.add(entry.getKey(), entry.getValue().getAddress().toString());
        }
        server.add("servers", servers);

        JsonObject machine = new JsonObject().add("java", System.getProperty("java.version"))
                .add("system", System.getProperty("os.name"));

        JsonObject config = new JsonObject().add("prefix", ProjectRetile.getInstance().getConfig().getString("General.prefix"))
                .add("locale", ProjectRetile.getInstance().getConfig().getString("General.locale"))
                .add("usebungeecordforuuid", ProjectRetile.getInstance().getConfig().getBoolean("General.usebungeecordforuuid"))
                .add("cooldown", ProjectRetile.getInstance().getConfig().getLong("General.cooldown"))
                .add("clickableMsgs", ProjectRetile.getInstance().getConfig().getBoolean("General.clickablemessages"))
                .add("dateFormat", ProjectRetile.getInstance().getConfig().getString("General.dateformat"))
                .add("updater", ProjectRetile.getInstance().getConfig().getBoolean("General.updater"))
                .add("revision", ProjectRetile.getInstance().getConfig().getLong("General.revision"))
                .add("minPoolIdle", ProjectRetile.getInstance().getConfig().getLong("Pools.minpoolidlesize"))
                .add("maxPoolSize", ProjectRetile.getInstance().getConfig().getLong("Pools.maxpoolsize"))
                .add("poolTimeout", ProjectRetile.getInstance().getConfig().getLong("Pools.timeout"))
                .add("reportAliases", Joiner.on(", ").join(ProjectRetile.getInstance().getConfig().getList("Aliases.report")))
                .add("reportsAliases", Joiner.on(", ").join(ProjectRetile.getInstance().getConfig().getList("Aliases.listreports")))
                .add("toggleAliases", Joiner.on(", ").join(ProjectRetile.getInstance().getConfig().getList("Aliases.togglereports")))
                .add("infoAliases", Joiner.on(", ").join(ProjectRetile.getInstance().getConfig().getList("Aliases.reportinfo")))
                .add("queueAliases", Joiner.on(", ").join(ProjectRetile.getInstance().getConfig().getList("Aliases.waitingqueue")));

        JsonObject dbConfig = new JsonObject().add("type", ProjectRetile.getInstance().getDbConfig().getString("Database.type"));
        if(ProjectRetile.getInstance().getDbConfig().getString("Database.type").equalsIgnoreCase("MYSQL")) {
            dbConfig.add("adress", ProjectRetile.getInstance().getDbConfig().getString("Database.MySQL.adress"))
            .add("port", ProjectRetile.getInstance().getDbConfig().getLong("Database.MySQL.port"))
            .add("database", ProjectRetile.getInstance().getDbConfig().getString("Database.MySQL.database"))
            .add("username", ProjectRetile.getInstance().getDbConfig().getString("Database.MySQL.username"))
            .add("password", ProjectRetile.getInstance().getDbConfig().getString("Database.MySQL.password"));
        } else {
            dbConfig.add("file", ProjectRetile.getInstance().getDbConfig().getString("Database.SQLite.file"));
        }

        JsonObject blacklist = new JsonObject().add("blacklist", Joiner.on(", ").join(ProjectRetile.getInstance().getBlacklist().getList("blacklist")));

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

        JsonObject database = new JsonObject().add("conntected", ProjectRetile.getInstance().getDatabase().isConnected())
                .add("type", ProjectRetile.getInstance().getDatabase() instanceof MySQL ? "MySQL" : "SQLite")
                .add("tableRetile", ProjectRetile.getInstance().getDatabase().doesTableExist("retile"))
                .add("tableQueue", ProjectRetile.getInstance().getDatabase().doesTableExist("queue"))
                .add("tableVersion", ProjectRetile.getInstance().getDatabase().doesTableExist("version"));

        JsonObject cache = new JsonObject();
        if(ProjectRetile.getInstance().getCache() instanceof Offline) {
            cache.add("resolver", "BungeeCord");
            cache.add("values", "empty");
        } else {
            cache.add("resolver", "mcapi.ca");
            JsonObject values = new JsonObject();
            for(Map.Entry<UUID, String> entry : ((McAPICanada) ProjectRetile.getInstance().getCache()).cache.asMap().entrySet()) {
                values.add(entry.getKey().toString(), entry.getValue());
            }
            cache.add("values", values);
        }

        return new JsonObject().add("retile", retilePlugin)
                .add("server", server)
                .add("machine", machine)
                .add("config", config)
                .add("dbConfig1", dbConfig)
                .add("blacklist", blacklist)
                .add("plugins", plugins)
                .add("database", database)
                .add("cache", cache);
    }

}
