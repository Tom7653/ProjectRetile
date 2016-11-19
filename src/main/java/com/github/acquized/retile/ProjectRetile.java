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
package com.github.acquized.retile;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.github.acquized.retile.api.RetileAPI;
import com.github.acquized.retile.api.RetileAPIProvider;
import com.github.acquized.retile.cache.Cache;
import com.github.acquized.retile.cache.impl.McAPICanada;
import com.github.acquized.retile.cache.impl.Offline;
import com.github.acquized.retile.commands.InfoCommand;
import com.github.acquized.retile.commands.ListReportsCommand;
import com.github.acquized.retile.commands.QueueCommand;
import com.github.acquized.retile.commands.ReportCommand;
import com.github.acquized.retile.commands.RetileCommand;
import com.github.acquized.retile.commands.ToggleCommand;
import com.github.acquized.retile.config.Blacklist;
import com.github.acquized.retile.config.Config;
import com.github.acquized.retile.config.DBConfig;
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

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
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
    @Getter private Logger log = LoggerFactory.getLogger(ProjectRetile.class);
    @Getter @Setter(onParam = @__(@NonNull)) private Database database;
    @Getter private Blacklist blacklist;
    @Getter private DBConfig dbConfig;
    @Getter private RetileAPI api;
    @Getter private Config config;
    @Getter private Cache cache;
    @Getter private I18n i18n;

    @Override
    public void onEnable() {
        instance = this;
        if(!isBungeeUtilInstalled()) {
            log.error("Could not load BungeeUtil. Please install it and start the Proxy Server again.");
            Utility.disablePlugin(this);
            return;
        }
        ProxyServer.getInstance().getPluginManager().registerListener(this, new JoinProtection()); // High priority for causing no errors with BungeeUtil
        loadConfigs();
        prefix = Utility.format(config.prefix);
        i18n = new I18n();
        i18n.load();
        if((ProxyServer.getInstance().getConfig().isOnlineMode()) && (!config.forceOfflineUUID) && (isMcAPIOnline())) {
            cache = new McAPICanada();
        } else {
            cache = new Offline();
        }
        try {
            if(dbConfig.jdbcURL.contains("mysql")) {
                database = new MySQL(dbConfig.jdbcURL, dbConfig.username, dbConfig.password.toCharArray());
                log.info("Using MySQL Connection...");
            } else {
                database = new SQLite(dbConfig.jdbcURL);
                log.info("Using SQLite Connection...");
            }
            database.connect();
            database.setup();
        } catch (Exception ex) { // maybe change this, catching every Exception is always bad
            log.error("Could not connect to MySQL / SQLite Database! Did you enter the correct Details?");
            log.debug(ex.getClass().getName() + ": " + ex.getMessage(), ex);
            Utility.disablePlugin(this);
            return;
        }
        Cooldown.setInstance(new Cooldown());
        Notifications.setInstance(new Notifications());
        api = new RetileAPIProvider();
        registerListeners(ProxyServer.getInstance().getPluginManager());
        registerCommands(ProxyServer.getInstance().getPluginManager());
        log.info("ProjectRetile v{} has been enabled.", getDescription().getVersion());
        if(config.updater)
            Updater.start();
    }

    @Override
    public void onDisable() {
        ProxyServer.getInstance().getScheduler().cancel(this);
        try {
            database.disconnect();
        } catch (SQLException ex) {
            log.error("Could not disconnect from the MySQL / SQLite Database! Please force end the Java Process.");
            ProjectRetile.getInstance().getLog().debug(ex.getClass().getName() + ": " + ex.getMessage(), ex);
        }
        instance = null;
        log.info("ProjectRetile v{} has been disabled.", getDescription().getVersion());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadConfigs() {
        // config.yml
        try {
            File file;
            config = new Config(file = new File(getDataFolder(), "config.yml"));
            config.init();
            if (!config.version.equalsIgnoreCase(getDescription().getVersion())) {
                file.delete();
                config.init();
            }
        } catch (InvalidConfigurationException ex) {
            log.error("Could not load config.yml File - Please check for Errors", ex);
        }

        // database.yml
        try {
            dbConfig = new DBConfig(new File(getDataFolder(), "database.yml"));
            dbConfig.init();
        } catch (InvalidConfigurationException ex) {
            log.error("Could not load database.yml File - Please check for Errors", ex);
        }

        // blacklist.yml
        try {
            blacklist = new Blacklist(new File(getDataFolder(), "blacklist.yml"));
            blacklist.init();
        } catch (InvalidConfigurationException ex) {
            log.error("Could not load blacklist.yml File - Please check for Errors", ex);
        }
    }

    private boolean isBungeeUtilInstalled() {
        for(Plugin p : ProxyServer.getInstance().getPluginManager().getPlugins()) {
            if(p.getDescription().getName().equals("BungeeUtil")) {
                if(!p.getDescription().getVersion().startsWith("2")) {
                    return true;
                } else {
                    log.error("ProjectRetile has detected that you're using BungeeUtil v2.x(-SNAPSHOT)");
                    log.error("BungeeUtil's API v2 contains some big changes that aren't supported (yet!)");
                    log.error("Please downgrade to API v1 or wait until ProjectRetile gets the API v2 update");
                    Utility.disablePlugin(this);
                    return false;
                }
            }
        }
        return false;
    }

    private void registerListeners(PluginManager pm) {
        pm.registerListener(this, new Disconnect());
        pm.registerListener(this, new PostLogin());
    }

    private void registerCommands(PluginManager pm) {
        pm.registerCommand(this, new InfoCommand());
        pm.registerCommand(this, new ListReportsCommand());
        pm.registerCommand(this, new QueueCommand());
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

            JsonObject obj = Json.parse(new InputStreamReader(conn.getInputStream())).asObject();
            return obj.get("error") == null;
        });
        ProxyServer.getInstance().getScheduler().runAsync(this, task);
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException ex) {
            return false;
        }
    }

}
