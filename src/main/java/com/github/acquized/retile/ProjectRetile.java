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

import com.github.acquized.retile.api.RetileAPI;
import com.github.acquized.retile.api.RetileAPIProvider;
import com.github.acquized.retile.cache.Cache;
import com.github.acquized.retile.config.Config;
import com.github.acquized.retile.config.DBConfig;
import com.github.acquized.retile.i18n.I18n;
import com.github.acquized.retile.sql.Database;
import com.github.acquized.retile.sql.impl.MySQL;
import com.github.acquized.retile.utils.Utility;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import lombok.Getter;

public class ProjectRetile extends Plugin {

    public static String prefix = Utility.RED + "> " + Utility.GRAY;
    @Getter private static ProjectRetile instance;
    @Getter private Logger log = LoggerFactory.getLogger(ProjectRetile.class);
    @Getter private Database database;
    @Getter private DBConfig dbConfig;
    @Getter private RetileAPI api;
    @Getter private Config config;

    @Override
    public void onEnable() {
        instance = this;
        loadConfigs();
        prefix = Utility.format(config.prefix);
        new I18n().load();
        Cache.setInstance(new Cache());
        try {
            database = new MySQL(dbConfig.adress, dbConfig.port, dbConfig.database, dbConfig.username, dbConfig.password.toCharArray());
            database.connect();
            database.setup();
        } catch (SQLException ex) {
            log.error("Could not connect to / setup MySQL Database! Did you enter the correct Details?", ex);
            return;
        }
        api = new RetileAPIProvider();
        registerListeners(ProxyServer.getInstance().getPluginManager());
        registerCommands(ProxyServer.getInstance().getPluginManager());
        log.info("ProjectRetile v{} has been enabled.", getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        instance = null;
        log.info("ProjectRetile v{} has been disabled.", getDescription().getVersion());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadConfigs() {
        // config.cfg
        try {
            config = new Config();
            File file;
            config.initialize(file = new File(getDataFolder(), "config.cfg"));
            if (!config.version.equalsIgnoreCase(getDescription().getVersion())) {
                file.delete();
                config.initialize(new File(getDataFolder(), "config.cfg"));
            }
        } catch (IOException ex) {
            log.error("Could not load config.cfg File - Please check for Errors", ex);
        }

        // database.cfg
        try {
            dbConfig = new DBConfig();
            dbConfig.initialize(new File(getDataFolder(), "database.cfg"));
        } catch (IOException ex) {
            log.error("Could not load database.cfg File - Please check for Errors", ex);
        }
    }

    private void registerListeners(PluginManager pm) {

    }

    private void registerCommands(PluginManager pm) {

    }

}
