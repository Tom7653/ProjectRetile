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
package com.github.acquized.retile.utils;

import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import com.github.acquized.retile.ProjectRetile;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        private long freeMemory;
        private long maxMemory;
        private long totalMemory;

    }

    @AllArgsConstructor
    public static class Config {

    }

    @AllArgsConstructor
    public static class DBConfig {

    }

    @AllArgsConstructor
    public static class PluginInfo {

        String version;
        String main;
        String author;
        String hash;

    }

    public static Dump create() throws IOException {

        List<SubServer> servers = new ArrayList<>();
        for(Map.Entry<String, ServerInfo> entry : ProxyServer.getInstance().getServers().entrySet()) {
            servers.add(new SubServer(entry.getKey(), entry.getValue().getAddress().toString()));
        }

        Map<String, PluginInfo> plugins = new HashMap<>();
        for(Plugin p : ProxyServer.getInstance().getPluginManager().getPlugins()) {
            plugins.put(p.getDescription().getName(), new PluginInfo(
                    p.getDescription().getVersion(),
                    p.getDescription().getMain(),
                    p.getDescription().getAuthor(),
                    Files.hash(p.getDescription().getFile(), Hashing.md5()).toString()
            ));
        }

        Dump dump = new Dump(
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
                        Runtime.getRuntime().freeMemory(), Runtime.getRuntime().maxMemory(), Runtime.getRuntime().totalMemory()),

                // Configuration
                null,

                // Database Configuration
                null,

                // Blacklist
                Joiner.on(',').join(ProjectRetile.getInstance().getBlacklist().getList("blacklist")),

                // Plugins
                plugins
        );
        return dump;
    }

}
