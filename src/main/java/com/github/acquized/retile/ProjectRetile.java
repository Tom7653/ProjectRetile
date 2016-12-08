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
package com.github.acquized.retile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.github.acquized.retile.api.RetileAPI;
import com.github.acquized.retile.api.RetileAPIProvider;
import com.github.acquized.retile.cache.Cache;
import com.github.acquized.retile.cache.impl.McAPICanada;
import com.github.acquized.retile.cache.impl.Mojang;
import com.github.acquized.retile.cache.impl.Offline;
import com.github.acquized.retile.commands.InfoCommand;
import com.github.acquized.retile.commands.ListReportsCommand;
import com.github.acquized.retile.commands.QueueCommand;
import com.github.acquized.retile.commands.ReportCommand;
import com.github.acquized.retile.commands.RetileCommand;
import com.github.acquized.retile.commands.ToggleCommand;
import com.github.acquized.retile.cooldown.Cooldown;
import com.github.acquized.retile.i18n.I18n;
import com.github.acquized.retile.listeners.Disconnect;
import com.github.acquized.retile.listeners.JoinProtection;
import com.github.acquized.retile.listeners.PostLogin;
import com.github.acquized.retile.notifications.Notifications;
import com.github.acquized.retile.sql.Database;
import com.github.acquized.retile.sql.impl.MySQL;
import com.github.acquized.retile.sql.impl.SQLite;
import com.github.acquized.retile.updater.Updater;
import com.github.acquized.retile.utils.Utility;
import com.moandjiezana.toml.Toml;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.net.ssl.HttpsURLConnection;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import static com.github.acquized.retile.utils.Utility.GRAY;
import static com.github.acquized.retile.utils.Utility.RED;

public class ProjectRetile extends Plugin {

    public static String prefix = RED + "> " + GRAY;
    @Getter private static ProjectRetile instance;
    @Getter private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Getter private Logger log = LoggerFactory.getLogger(ProjectRetile.class);
    @Getter @Setter(onParam = @__(@NonNull)) private Database database;
    @Getter private JsonParser jsonParser = new JsonParser();
    @Getter private Toml blacklist;
    @Getter private Toml dbConfig;
    @Getter private RetileAPI api;
    @Getter private Toml config;
    @Getter private Cache cache;
    @Getter private I18n i18n;

    @Override
    public void onEnable() {
        instance = this;
        if(!isBungeeUtilInstalled()) {
            Utility.disablePlugin(this);
            return;
        }
        ProxyServer.getInstance().getPluginManager().registerListener(this, new JoinProtection()); // High priority for causing no errors with BungeeUtil
        loadConfigs();
        prefix = Utility.format(config.getString("General.prefix"));
        i18n = new I18n();
        i18n.load();
        if((ProxyServer.getInstance().getConfig().isOnlineMode()) && (!config.getBoolean("General.usebungeecordforuuid"))) {
            if(isMcAPIOnline()) {
                cache = new McAPICanada();
            } else {
                cache = new Mojang();
            }
        } else {
            cache = new Offline();
        }
        try {
            if(dbConfig.getString("Database.type").equalsIgnoreCase("MYSQL")) {
                database = new MySQL("jdbc:mysql://" + dbConfig.getString("Database.MySQL.adress") + ":" + dbConfig.getLong("Database.MySQL.port") + "/" + dbConfig.getString("Database.MySQL.database"), dbConfig.getString("Database.MySQL.username"), dbConfig.getString("Database.MySQL.password").toCharArray());
                log.info("Using MySQL connection...");
            } else {
                database = new SQLite("jdbc:sqlite:{0}{1}" + dbConfig.getString("Database.SQLite.file"));
                log.info("Using SQLite connection...");
            }
            database.connect();
            database.setup();
        } catch (Exception ex) { // maybe change this, catching every Exception is always bad
            log.error("Could not connect to MySQL / SQLite database! Did you enter the correct details?", ex);
            Utility.disablePlugin(this);
            return;
        }
        Cooldown.setInstance(new Cooldown());
        Notifications.setInstance(new Notifications());
        api = new RetileAPIProvider();
        registerListeners(ProxyServer.getInstance().getPluginManager());
        registerCommands(ProxyServer.getInstance().getPluginManager(), new SimpleDateFormat(getConfig().getString("General.dateformat")));
        log.info("ProjectRetile v{} has been enabled.", getDescription().getVersion());
        if(config.getBoolean("General.updater"))
            Updater.start();
    }

    @Override
    public void onDisable() {
        ProxyServer.getInstance().getScheduler().cancel(this);
        try {
            if((database != null) && (database.isConnected())) {
                database.disconnect();
            }
        } catch (SQLException ex) {
            log.error("Could not disconnect from the MySQL / SQLite database! Please force end the Java process.", ex);
        }
        instance = null;
        log.info("ProjectRetile v{} has been disabled.", getDescription().getVersion());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadConfigs() {
        // Directory
        if(!getDataFolder().isDirectory()) {
            getDataFolder().mkdirs();
        }

        // config.toml
        try {
            File file = new File(getDataFolder(), "config.toml");
            if(!file.exists()) {
                Files.copy(getResourceAsStream("config/config.toml"), file.toPath());
            }
            config = new Toml(new Toml().read(getResourceAsStream("config/config.toml"))).read(file);
        } catch (Exception ex) {
            log.error("Could not load config.toml file - Please check for errors", ex);
        }

        // database.toml
        try {
            File file = new File(getDataFolder(), "database.toml");
            if(!file.exists()) {
                Files.copy(getResourceAsStream("config/database.toml"), file.toPath());
            }
            dbConfig = new Toml(new Toml().read(getResourceAsStream("config/database.toml"))).read(file);
        } catch (Exception ex) {
            log.error("Could not load database.toml file - Please check for errors", ex);
        }

        // blacklist.toml
        try {
            File file = new File(getDataFolder(), "blacklist.toml");
            if(!file.exists()) {
                Files.copy(getResourceAsStream("config/blacklist.toml"), file.toPath());
            }
            blacklist = new Toml(new Toml().read(getResourceAsStream("config/blacklist.toml"))).read(file);
        } catch (Exception ex) {
            log.error("Could not load blacklist.toml file - Please check for errors", ex);
        }
    }

    private boolean isBungeeUtilInstalled() {
        for(Plugin p : ProxyServer.getInstance().getPluginManager().getPlugins()) {
            if(p.getDescription().getName().equals("BungeeUtil")) {
                try {
                    Class.forName("dev.wolveringer.bungeeutil.BungeeUtil");
                } catch (ClassNotFoundException ex) {
                    log.error("Could not load BungeeUtil. You are using a outdated or newer version than supported.");
                    log.error("ProjectRetile only supports BungeeUtil after v2.0.0. Please upgrade BungeeUtil or download a older version of ProjectRetile.");
                    return false;
                }
                log.info("BungeeUtil detected.");
                return true;
            }
        }
        log.error("Could not load BungeeUtil. Please install it and start the proxy server again.");
        return false;
    }

    private void registerListeners(PluginManager pm) {
        pm.registerListener(this, new Disconnect());
        pm.registerListener(this, new PostLogin());
    }

    private void registerCommands(PluginManager pm, SimpleDateFormat format) {
        pm.registerCommand(this, new InfoCommand(format));
        pm.registerCommand(this, new ListReportsCommand(format));
        pm.registerCommand(this, new QueueCommand(format));
        pm.registerCommand(this, new ReportCommand());
        pm.registerCommand(this, new RetileCommand());
        pm.registerCommand(this, new ToggleCommand());
    }

    private boolean isMcAPIOnline() {
        FutureTask<Boolean> task = new FutureTask<>(() -> {
            URL url = new URL("https://mcapi.ca/profile/Notch?" + System.currentTimeMillis());
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.addRequestProperty("User-Agent", "ProjectRetile v" + ProjectRetile.getInstance().getDescription().getVersion());
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setDoOutput(true);

            JsonObject obj = jsonParser.parse(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
            return obj.get("error").isJsonNull();
        });
        ProxyServer.getInstance().getScheduler().runAsync(this, task);
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException ex) {
            return false;
        }
    }

}
