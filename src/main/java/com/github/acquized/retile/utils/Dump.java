/*
 * Copyright 2016 Acquized
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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

import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.api.RetileAPIException;
import com.github.acquized.retile.cache.impl.McAPICanada;
import com.github.acquized.retile.cache.impl.Mojang;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Dump {

    public final RetilePlugin retilePlugin;
    public final Server server;
    public final List<SubServer> servers;
    public final Machine machine;
    public final Config config;
    public final DBConfig dbConfig;
    public final String blacklist;
    public final Map<String, PluginInfo> plugins;
    public final Database database;
    public final Cache cache;

    @AllArgsConstructor
    public static class RetilePlugin {

        private String name;
        private String version;
        private String author;
        private String main;
        private String hash;

    }

    @AllArgsConstructor
    public static class Server {

        private String name;
        private String version;

    }

    @AllArgsConstructor
    public static class SubServer {

        private String name;
        private String adress;

    }

    @AllArgsConstructor
    public static class Machine {

        private String java;
        private String system;
        private String freeMemory;
        private String maxMemory;
        private String totalMemory;

    }

    public static class Config {

        General General;
        Pools Pools;
        Aliases Aliases;

        public static class General {

            String prefix;
            String locale;
            boolean usebungeecordforuuid;
            int cooldown;
            boolean clickablemessages;
            String dateformat;
            boolean updater;
            int revision;

        }

        public static class Pools {

            int minpoolidlesize;
            int maxpoolsize;
            long timeout;

        }

        public static class Aliases {

            String[] report;
            String[] listreports;
            String[] togglereports;
            String[] reportinfo;
            String[] waitingqueue;

        }

    }

    public static class DBConfig {

        Database database;

        public static class Database {

            String type;
            MySQL MySQL;
            SQLite SQLite;

            public static class MySQL {

                String adress;
                int port;
                String database;
                String username;
                String password;

            }

            public static class SQLite {

                String file;

            }

        }

    }

    @AllArgsConstructor
    public static class PluginInfo {

        String version;
        String main;
        String author;
        String hash;

    }

    @AllArgsConstructor
    public static class Database {

        boolean connected;
        String type;
        boolean tableRetile;
        boolean tableQueue;
        int retileSize;
        int queueSize;

    }

    @AllArgsConstructor
    public static class Cache {

        String resolver;
        Map<UUID, String> entries;

    }

    public static Dump create() throws IOException, RetileAPIException {

        List<SubServer> servers = new ArrayList<>();
        for(Map.Entry<String, ServerInfo> entry : ProxyServer.getInstance().getServers().entrySet()) {
            servers.add(new SubServer(entry.getKey(), entry.getValue().getAddress().toString()));
        }

        Map<String, PluginInfo> plugins = new HashMap<>();
        for(Plugin p : ProxyServer.getInstance().getPluginManager().getPlugins()) {
            if(!p.getDescription().getAuthor().equalsIgnoreCase("SpigotMC")) {
                plugins.put(p.getDescription().getName(), new PluginInfo(
                        p.getDescription().getVersion(),
                        p.getDescription().getMain(),
                        p.getDescription().getAuthor(),
                        Files.hash(p.getDescription().getFile(), Hashing.md5()).toString()
                ));
            }
        }

        Map<UUID, String> cache = new HashMap<>();
        if(ProjectRetile.getInstance().getCache() instanceof McAPICanada) {
            cache = ((McAPICanada) ProjectRetile.getInstance().getCache()).getCache().asMap();
        } else if(ProjectRetile.getInstance().getCache() instanceof Mojang) {
            cache = ((Mojang) ProjectRetile.getInstance().getCache()).getCache().asMap();
        } else {
            cache.put(UUID.randomUUID(), "Empty");
        }

        try {
            return new Dump(
                    // Retile Plugin Information
                    new RetilePlugin(ProjectRetile.getInstance().getDescription().getName(),
                            ProjectRetile.getInstance().getDescription().getVersion(),
                            ProjectRetile.getInstance().getDescription().getAuthor(),
                            ProjectRetile.getInstance().getDescription().getMain(),
                            Files.hash(ProjectRetile.getInstance().getDescription().getFile(), Hashing.md5()).toString()),

                    // Server Information
                    new Server(ProxyServer.getInstance().getName(), ProxyServer.getInstance().getVersion()),

                    // Servers available thought BungeeCord
                    servers,

                    // Machine on which BungeeCord is running
                    new Machine(System.getProperty("java.version"), System.getProperty("os.name"),
                            Utility.convertToReadableString(Runtime.getRuntime().freeMemory()),
                            Utility.convertToReadableString(Runtime.getRuntime().maxMemory()),
                            Utility.convertToReadableString(Runtime.getRuntime().totalMemory())),

                    // Configuration
                    ProjectRetile.getInstance().getConfig().to(Config.class),

                    // Database Configuration
                    ProjectRetile.getInstance().getDbConfig().to(DBConfig.class),

                    // Blacklist
                    Joiner.on(", ").join(ProjectRetile.getInstance().getBlacklist().getList("blacklist")),

                    // Plugins
                    plugins,

                    // Database
                    new Database(ProjectRetile.getInstance().getDatabase().isConnected(),
                            ProjectRetile.getInstance().getDatabase().getClass().getSimpleName().replace(".class", ""),
                            ProjectRetile.getInstance().getDatabase().doesTableExist("retile"),
                            ProjectRetile.getInstance().getDatabase().doesTableExist("queue"),
                            ProjectRetile.getInstance().getApi().getAllReports().length,
                            ProjectRetile.getInstance().getApi().getWaitingReports().length),

                    // Cache
                    new Cache(ProjectRetile.getInstance().getCache().getClass().getSimpleName().replace(".class", ""),
                            cache)

            );
        } catch (SQLException | RetileAPIException ex) {
            throw new RetileAPIException("Could not create Dump", ex);
        }
    }

}
