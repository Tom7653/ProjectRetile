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

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.github.acquized.retile.api.RetileAPI;
import com.github.acquized.retile.cache.Cache;
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

import org.mcstats.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import static com.github.acquized.retile.utils.Utility.GRAY;
import static com.github.acquized.retile.utils.Utility.RED;

public class ProjectRetile extends Plugin {

    public static String prefix = RED + "> " + GRAY;
    @Getter private Logger log = LoggerFactory.getLogger(ProjectRetile.class);
    @Getter private static Injector injector;
    @Getter @Setter(onParam = @__(@NonNull)) private Database database;
    @Getter private Blacklist blacklist;
    @Getter private DBConfig dbConfig;
    @Getter private Config config;

    @Override
    public void onEnable() {
        injector = Guice.createInjector(new Injection());
        if(!isBungeeUtilInstalled()) {
            log.error("Could not load BungeeUtil. Please install it and start the Proxy Server again.");
            Utility.disablePlugin(this);
            return;
        }
        ProxyServer.getInstance().getPluginManager().registerListener(this, new JoinProtection()); // High priority for causing no errors with BungeeUtil
        loadConfigs();
        prefix = Utility.format(config.prefix);
        injector.getInstance(I18n.class).load();
        Guice.createInjector(new Injection.CacheInjection(injector.getInstance(ProjectRetile.class)));
        try {
            if(dbConfig.jdbcURL.contains("mysql")) {
                database = new MySQL(dbConfig.jdbcURL, dbConfig.username, dbConfig.password.toCharArray(), injector.getInstance(ProjectRetile.class));
                log.info("Using MySQL Connection...");
            } else {
                database = new SQLite(injector.getInstance(ProjectRetile.class), dbConfig.jdbcURL);
                log.info("Using SQLite Connection...");
            }
            database.connect();
            database.setup();
        } catch (SQLException ex) {
            log.error("Could not connect to MySQL / SQLite Database! Did you enter the correct Details?", ex);
            Utility.disablePlugin(this);
            return;
        }
        Cooldown.setInstance(new Cooldown(injector.getInstance(ProjectRetile.class)));
        Notifications.setInstance(new Notifications(injector.getInstance(ProjectRetile.class), injector.getInstance(Cache.class)));
        registerListeners(ProxyServer.getInstance().getPluginManager());
        registerCommands(ProxyServer.getInstance().getPluginManager());
        log.info("ProjectRetile v{} has been enabled.", getDescription().getVersion());
        try {
            Metrics metrics = new Metrics(this); // Maybe use alternative as soon as available: https://www.spigotmc.org/threads/beta-bstats-a-modern-alternative-to-mcstats.187881/
            addCustomGraphs(metrics);
            metrics.start();
        } catch (IOException ex) {
            log.warn("Could not submit statistics about the plugin to McStats.org", ex);
        }
        if(config.updater)
            Updater.start();
    }

    @Override
    public void onDisable() {
        ProxyServer.getInstance().getScheduler().cancel(this);
        try {
            database.disconnect();
        } catch (SQLException ex) {
            log.error("Could not disconnect from the MySQL / SQLite Database! Please force end the Java Process.", ex);
        }
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
                return true;
            }
        }
        return false;
    }

    private void registerListeners(PluginManager pm) {
        pm.registerListener(this, new Disconnect());
        pm.registerListener(this, new PostLogin(injector.getInstance(ProjectRetile.class), injector.getInstance(RetileAPI.class)));
    }

    private void registerCommands(PluginManager pm) {
        pm.registerCommand(this, new InfoCommand(injector.getInstance(ProjectRetile.class), injector.getInstance(RetileAPI.class), injector.getInstance(Cache.class)));
        pm.registerCommand(this, new ListReportsCommand(injector.getInstance(ProjectRetile.class), injector.getInstance(RetileAPI.class), injector.getInstance(Cache.class)));
        pm.registerCommand(this, new QueueCommand(injector.getInstance(ProjectRetile.class), injector.getInstance(RetileAPI.class), injector.getInstance(Cache.class)));
        pm.registerCommand(this, new ReportCommand(injector.getInstance(ProjectRetile.class), injector.getInstance(RetileAPI.class), injector.getInstance(Cache.class)));
        pm.registerCommand(this, new RetileCommand(injector.getInstance(ProjectRetile.class), injector.getInstance(I18n.class)));
        pm.registerCommand(this, new ToggleCommand(injector.getInstance(ProjectRetile.class)));
    }

    private void addCustomGraphs(Metrics metrics) {
        Metrics.Graph databaseGraph = metrics.createGraph("Database Type");
        databaseGraph.addPlotter(new Metrics.Plotter("MySQL") {
            @Override
            public int getValue() {
                if(database instanceof MySQL) {
                    return 1;
                }
                return 0;
            }
        });
        databaseGraph.addPlotter(new Metrics.Plotter("SQLite") {
            @Override
            public int getValue() {
                if(database instanceof SQLite) {
                    return 1;
                }
                return 0;
            }
        });
    }

}
