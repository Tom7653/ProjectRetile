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

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.WriterConfig;
import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.sql.impl.MySQL;
import com.github.acquized.retile.sql.impl.SQLite;
import com.github.acquized.retile.utils.DumpReport;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.github.acquized.retile.i18n.I18n.tl;
import static com.github.acquized.retile.utils.Utility.DARK_AQUA;
import static com.github.acquized.retile.utils.Utility.GRAY;
import static com.github.acquized.retile.utils.Utility.RED;
import static com.github.acquized.retile.utils.Utility.formatLegacy;

public class RetileCommand extends Command {

    public RetileCommand() {
        super("projectretile", null, "retile");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender.hasPermission("projectretile.general")) {
            if(args.length == 0) {
                sender.sendMessage(formatLegacy(RED + "> " + GRAY + "ProjectRetile v" + ProjectRetile.getInstance().getDescription().getVersion()));
                sender.sendMessage(formatLegacy(RED + "> " + GRAY + "GitHub: " + DARK_AQUA + "https://github.com/Acquized/ProjectRetile"));
                return;
            }
            if(args.length == 1) {
                if(args[0].equalsIgnoreCase("debug")) {
                    if(sender.hasPermission("projectretile.general.debug")) {
                        sender.sendMessage(formatLegacy(RED + "> " + GRAY + "ProjectRetile is using the " + DARK_AQUA + "Slf4j " + GRAY + "logger."));
                        sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Enabling the debug mode is not possible using just a command."));
                        sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Please visit the wiki: " + DARK_AQUA + "https://github.com/Acquized/ProjectRetile/wiki/enabling-debug"));
                        ProjectRetile.getInstance().getLog().debug("The debug mode has already been enabled.");
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
                            ProjectRetile.getInstance().getConfig().read(ProjectRetile.getInstance().getResourceAsStream("config/config.toml"));
                        } catch (Exception ex) {
                            sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Could not reload config.toml file. Please check for errors."));
                            return;
                        }
                        try {
                            ProjectRetile.getInstance().getBlacklist().read(ProjectRetile.getInstance().getResourceAsStream("config/blacklist.toml"));
                        } catch (Exception ex) {
                            sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Could not reload blacklist.toml file. Please check for errors."));
                            return;
                        }
                        ProjectRetile.getInstance().getI18n().load();
                        if(!(sender instanceof ProxiedPlayer)) {
                            try {
                                ProjectRetile.getInstance().getDatabase().disconnect();
                                ProjectRetile.getInstance().getDbConfig().read(ProjectRetile.getInstance().getResourceAsStream("config/database.toml"));
                                if(ProjectRetile.getInstance().getDbConfig().getString("Database.type").equalsIgnoreCase("MYSQL")) {
                                    ProjectRetile.getInstance().setDatabase(new MySQL("jdbc:mysql://" + ProjectRetile.getInstance().getDbConfig().getString("Database.MySQL.adress") + ":" + ProjectRetile.getInstance().getDbConfig().getDouble("Database.MySQL.port") + "/" + ProjectRetile.getInstance().getDbConfig().getString("Database.MySQL.database"), ProjectRetile.getInstance().getDbConfig().getString("Database.MySQL.username"), ProjectRetile.getInstance().getDbConfig().getString("Database.MySQL.password").toCharArray()));
                                    ProjectRetile.getInstance().getLog().info("Using MySQL connection...");
                                } else {
                                    ProjectRetile.getInstance().setDatabase(new SQLite("jdbc:sqlite:{0}{1}" + ProjectRetile.getInstance().getDbConfig().getString("Database.SQLite.file")));
                                    ProjectRetile.getInstance().getLog().info("Using SQLite connection...");
                                }
                                ProjectRetile.getInstance().getDatabase().connect();
                                ProjectRetile.getInstance().getDatabase().setup();
                            } catch (Exception ex) {
                                sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Could not reload database. Please force end the Java process."));
                                return;
                            }
                        } else {
                            sender.sendMessage(formatLegacy(RED + "> " + GRAY + "If you wish to reload the database, execute this command using the Console."));
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
                        ProxyServer.getInstance().getScheduler().runAsync(ProjectRetile.getInstance(), () -> {
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
                                ProjectRetile.getInstance().getLog().error("An error occured while contacting Hastebin.", ex);
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
