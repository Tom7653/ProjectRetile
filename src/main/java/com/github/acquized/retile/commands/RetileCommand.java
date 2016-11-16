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
package com.github.acquized.retile.commands;

import com.google.inject.Inject;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.WriterConfig;
import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.i18n.I18n;
import com.github.acquized.retile.sql.impl.MySQL;
import com.github.acquized.retile.sql.impl.SQLite;
import com.github.acquized.retile.utils.DumpReport;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;

import static com.github.acquized.retile.i18n.I18n.tl;
import static com.github.acquized.retile.utils.Utility.DARK_AQUA;
import static com.github.acquized.retile.utils.Utility.GRAY;
import static com.github.acquized.retile.utils.Utility.RED;
import static com.github.acquized.retile.utils.Utility.formatLegacy;

public class RetileCommand extends Command {

    private ProjectRetile retile;
    private I18n i18n;
    
    @Inject
    public RetileCommand(ProjectRetile retile, I18n i18n) {
        super("projectretile", null, "retile");
        this.retile = retile;
        this.i18n = i18n;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender.hasPermission("projectretile.general")) {
            if(args.length == 0) {
                sender.sendMessage(formatLegacy(RED + "> " + GRAY + "ProjectRetile v" + retile.getDescription().getVersion()));
                sender.sendMessage(formatLegacy(RED + "> " + GRAY + "GitHub: " + DARK_AQUA + "https://github.com/Acquized/ProjectRetile"));
                return;
            }
            if(args.length == 1) {
                if(args[0].equalsIgnoreCase("debug")) {
                    if(sender.hasPermission("projectretile.general.debug")) {
                        sender.sendMessage(formatLegacy(RED + "> " + GRAY + "ProjectRetile is using the " + DARK_AQUA + "Slf4j " + GRAY + "Logger."));
                        sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Enabling the Debug Modus is not possible using just a command."));
                        sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Please visit the Wiki: " + DARK_AQUA + "https://github.com/Acquized/ProjectRetile/wiki/enabling-debug"));
                        return;
                    } else {
                        sender.sendMessage(tl("ProjectRetile.General.NoPermission"));
                        return;
                    }
                }
                if(args[0].equalsIgnoreCase("reload")) {
                    if(sender.hasPermission("projectretile.general.reload")) {
                        // Players can only reload Config, Messages & Blacklist - console can reload Database
                        try {
                            retile.config.reload();
                        } catch (InvalidConfigurationException ex) {
                            sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Could not reload config.yml File. Please check for errors."));
                            return;
                        }
                        try {
                            retile.blacklist.reload();
                        } catch (InvalidConfigurationException ex) {
                            sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Could not reload blacklist.yml File. Please check for errors."));
                            return;
                        }
                        i18n.load();
                        if(!(sender instanceof ProxiedPlayer)) {
                            try {
                                retile.getDatabase().disconnect();
                                retile.dbConfig.reload();
                                if(retile.dbConfig.jdbcURL.contains("mysql")) {
                                    retile.setDatabase(new MySQL(retile.dbConfig.jdbcURL, retile.dbConfig.username, retile.dbConfig.password.toCharArray(), retile));
                                    retile.getLog().info("Using MySQL Connection...");
                                } else {
                                    retile.setDatabase(new SQLite(retile, retile.dbConfig.jdbcURL));
                                    retile.getLog().info("Using SQLite Connection...");
                                }
                                retile.getDatabase().connect();
                                retile.getDatabase().setup();
                            } catch (SQLException | InvalidConfigurationException ex) {
                                sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Could not reload Database. Please force end the Java Process."));
                                return;
                            }
                        } else {
                            sender.sendMessage(formatLegacy(RED + "> " + GRAY + "If you wish to reload the Database, execute this command using the console."));
                        }
                        sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Successfully reloaded."));
                        return;
                    } else {
                        sender.sendMessage(tl("ProjectRetile.General.NoPermission"));
                        return;
                    }
                }
                if(args[0].equalsIgnoreCase("dump")) {
                    if(sender.hasPermission("projectretile.general.dump")) {
                        ProxyServer.getInstance().getScheduler().runAsync(retile, () -> {
                            try {
                                URL url = new URL("http://hastebin.com/documents");
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestProperty("Content-Type", "text/plain");
                                conn.setRequestMethod("POST");
                                conn.setUseCaches(false);
                                conn.setDoOutput(true);

                                OutputStream out = conn.getOutputStream();
                                out.write(DumpReport.create().toString(WriterConfig.PRETTY_PRINT).getBytes("UTF-8"));
                                out.close();

                                JsonObject obj = Json.parse(new InputStreamReader(conn.getInputStream())).asObject();
                                sender.sendMessage(formatLegacy(RED + "> " + GRAY + "A dump has been successfully created. View it at " + DARK_AQUA + "http://hastebin.com/{0}", obj.get("key").asString()));
                            } catch (Exception ex) {
                                sender.sendMessage(formatLegacy(RED + "> " + GRAY + "A error occured. Please checkout the console."));
                                retile.getLog().error("An error occured while contacting Hastebin.", ex);
                            }
                        });
                        return;
                    } else {
                        sender.sendMessage(tl("ProjectRetile.General.NoPermission"));
                        return;
                    }
                }
            }
        } else {
            sender.sendMessage(tl("ProjectRetile.General.NoPermission"));
            return;
        }
        sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Syntax: " + DARK_AQUA + "/retile [Debug, Reload]"));
    }

}
