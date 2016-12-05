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
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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

    // TODO: Maybe change this to the reason GSON is better than minimal json (object json convertation)

    public static JsonObject create() throws IllegalAccessException, SQLException, IOException {
        JsonObject retilePlugin = new JsonObject();
        retilePlugin.add("name", new JsonPrimitive(ProjectRetile.getInstance().getDescription().getName()));
        retilePlugin.add("version", new JsonPrimitive(ProjectRetile.getInstance().getDescription().getVersion()));
        retilePlugin.add("author", new JsonPrimitive(ProjectRetile.getInstance().getDescription().getAuthor()));
        retilePlugin.add("main", new JsonPrimitive(ProjectRetile.getInstance().getDescription().getMain()));
        retilePlugin.add("hash", new JsonPrimitive(Files.hash(ProjectRetile.getInstance().getDescription().getFile(), Hashing.md5()).toString()));

        JsonObject server = new JsonObject();
        server.add("name", new JsonPrimitive(ProxyServer.getInstance().getName()));
        server.add("version", new JsonPrimitive(ProxyServer.getInstance().getVersion()));

        JsonObject servers = new JsonObject();
        for (Map.Entry<String, ServerInfo> entry : ProxyServer.getInstance().getServers().entrySet()) {
            servers.add(entry.getKey(), new JsonPrimitive(entry.getValue().getAddress().toString()));
        }
        server.add("servers", servers);

        JsonObject machine = new JsonObject();
        machine.add("java", new JsonPrimitive(System.getProperty("java.version")));
        machine.add("system", new JsonPrimitive(System.getProperty("os.name")));

        JsonObject config = new JsonObject();
        config.add("prefix", new JsonPrimitive(ProjectRetile.getInstance().getConfig().getString("General.prefix")));
        config.add("locale", new JsonPrimitive(ProjectRetile.getInstance().getConfig().getString("General.locale")));
        config.add("usebungeecordforuuid", new JsonPrimitive(ProjectRetile.getInstance().getConfig().getBoolean("General.usebungeecordforuuid")));
        config.add("cooldown", new JsonPrimitive(ProjectRetile.getInstance().getConfig().getLong("General.cooldown")));
        config.add("clickableMsgs", new JsonPrimitive(ProjectRetile.getInstance().getConfig().getBoolean("General.clickablemessages")));
        config.add("dateFormat", new JsonPrimitive(ProjectRetile.getInstance().getConfig().getString("General.dateformat")));
        config.add("updater", new JsonPrimitive(ProjectRetile.getInstance().getConfig().getBoolean("General.updater")));
        config.add("revision", new JsonPrimitive(ProjectRetile.getInstance().getConfig().getLong("General.revision")));
        config.add("minPoolIdle", new JsonPrimitive(ProjectRetile.getInstance().getConfig().getLong("Pools.minpoolidlesize")));
        config.add("maxPoolSize", new JsonPrimitive(ProjectRetile.getInstance().getConfig().getLong("Pools.maxpoolsize")));
        config.add("poolTimeout", new JsonPrimitive(ProjectRetile.getInstance().getConfig().getLong("Pools.timeout")));
        config.add("reportAliases", new JsonPrimitive(Joiner.on(", ").join(ProjectRetile.getInstance().getConfig().getList("Aliases.report"))));
        config.add("reportsAliases", new JsonPrimitive(Joiner.on(", ").join(ProjectRetile.getInstance().getConfig().getList("Aliases.listreports"))));
        config.add("toggleAliases", new JsonPrimitive(Joiner.on(", ").join(ProjectRetile.getInstance().getConfig().getList("Aliases.togglereports"))));
        config.add("infoAliases", new JsonPrimitive(Joiner.on(", ").join(ProjectRetile.getInstance().getConfig().getList("Aliases.reportinfo"))));
        config.add("queueAliases", new JsonPrimitive(Joiner.on(", ").join(ProjectRetile.getInstance().getConfig().getList("Aliases.waitingqueue"))));

        JsonObject dbConfig = new JsonObject();
        dbConfig.add("type", new JsonPrimitive(ProjectRetile.getInstance().getDbConfig().getString("Database.type")));
        if (ProjectRetile.getInstance().getDbConfig().getString("Database.type").equalsIgnoreCase("MYSQL")) {
            dbConfig.add("adress", new JsonPrimitive(ProjectRetile.getInstance().getDbConfig().getString("Database.MySQL.adress")));
            dbConfig.add("port", new JsonPrimitive(ProjectRetile.getInstance().getDbConfig().getLong("Database.MySQL.port")));
            dbConfig.add("database", new JsonPrimitive(ProjectRetile.getInstance().getDbConfig().getString("Database.MySQL.database")));
            dbConfig.add("username", new JsonPrimitive(ProjectRetile.getInstance().getDbConfig().getString("Database.MySQL.username")));
            dbConfig.add("password", new JsonPrimitive(ProjectRetile.getInstance().getDbConfig().getString("Database.MySQL.password")));
        } else {
            dbConfig.add("file", new JsonPrimitive(ProjectRetile.getInstance().getDbConfig().getString("Database.SQLite.file")));
        }

        JsonObject blacklist = new JsonObject();
        blacklist.add("blacklist", new JsonPrimitive(Joiner.on(", ").join(ProjectRetile.getInstance().getBlacklist().getList("blacklist"))));

        JsonObject plugins = new JsonObject();
        ProxyServer.getInstance().getPluginManager().getPlugins().stream().filter(p -> !p.getDescription().getAuthor().equals("SpigotMC")).forEach(p -> {
            try {
                JsonObject tmp = new JsonObject();
                tmp.add("version", new JsonPrimitive(p.getDescription().getVersion()));
                tmp.add("main", new JsonPrimitive(p.getDescription().getMain()));
                tmp.add("author", new JsonPrimitive(p.getDescription().getAuthor()));
                tmp.add("hash", new JsonPrimitive(Files.hash(p.getDescription().getFile(), Hashing.md5()).toString()));

                plugins.add(p.getDescription().getName(), tmp);
            } catch (IOException ex) {
                ProjectRetile.getInstance().getLog().error("Hashing for Plugin \"" + p.getDescription().getName() + "\" failed. Dump may be incomplete.");
            }
        });

        JsonObject database = new JsonObject();
        database.add("conntected", new JsonPrimitive(ProjectRetile.getInstance().getDatabase().isConnected()));
        database.add("type", new JsonPrimitive(ProjectRetile.getInstance().getDatabase() instanceof MySQL ? "MySQL" : "SQLite"));
        database.add("tableRetile", new JsonPrimitive(ProjectRetile.getInstance().getDatabase().doesTableExist("retile")));
        database.add("tableQueue", new JsonPrimitive(ProjectRetile.getInstance().getDatabase().doesTableExist("queue")));
        database.add("tableVersion", new JsonPrimitive(ProjectRetile.getInstance().getDatabase().doesTableExist("version")));

        JsonObject cache = new JsonObject();
        if (ProjectRetile.getInstance().getCache() instanceof Offline) {
            cache.add("resolver", new JsonPrimitive("BungeeCord"));
            cache.add("values", new JsonPrimitive("empty"));
        } else {
            cache.add("resolver", new JsonPrimitive("mcapi.ca"));
            JsonObject values = new JsonObject();
            for (Map.Entry<UUID, String> entry : ((McAPICanada) ProjectRetile.getInstance().getCache()).cache.asMap().entrySet()) {
                values.add(entry.getKey().toString(), new JsonPrimitive(entry.getValue()));
            }
            cache.add("values", values);
        }

        JsonObject finalObj = new JsonObject();
        finalObj.add("retile", retilePlugin);
        finalObj.add("server", server);
        finalObj.add("machine", machine);
        finalObj.add("config", config);
        finalObj.add("dbConfig", dbConfig);
        finalObj.add("blacklist", blacklist);
        finalObj.add("plugins", plugins);
        finalObj.add("database", database);
        finalObj.add("cache", cache);

        return finalObj;
    }

}
