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
package com.github.acquized.retile.commands;

import com.google.gson.JsonObject;

import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.api.RetileAPIException;
import com.github.acquized.retile.utils.Dump;
import com.github.acquized.retile.utils.Threads;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Console;
import com.sk89q.minecraft.util.commands.NestedCommand;

import net.md_5.bungee.api.CommandSender;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.github.acquized.retile.utils.Utility.DARK_AQUA;
import static com.github.acquized.retile.utils.Utility.GRAY;
import static com.github.acquized.retile.utils.Utility.RED;
import static com.github.acquized.retile.utils.Utility.formatLegacy;

public class RetileCommand {

    @Console
    @CommandPermissions({ "projectretile.general.reload" })
    @Command(aliases = { "reload", "rel" }, desc = "Reloads ProjectRetile's config files", usage = "reload", max = 0)
    public static void onSubReload(CommandSender sender, CommandContext args) throws CommandException {
        Threads.async(() -> {
            // config.toml
            try {
                ProjectRetile.getInstance().getConfig().read(ProjectRetile.getInstance().getResourceAsStream("config/config.toml"));
            } catch (IllegalStateException ex) {
                ProjectRetile.getInstance().getLog().error("Could not reload config.toml.", ex);
                Threads.sync(() -> sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Could not reload config.toml.")));
                return;
            }

            // blacklist.toml
            try {
                ProjectRetile.getInstance().getBlacklist().read(ProjectRetile.getInstance().getResourceAsStream("config/blacklist.toml"));
            } catch (IllegalStateException ex) {
                ProjectRetile.getInstance().getLog().error("Could not reload blacklist.toml.", ex);
                Threads.sync(() -> sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Could not reload blacklist.toml.")));
                return;
            }

            Threads.sync(() -> sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Successfully reloaded Config and Blacklist. Changes successfully applied.")));
            Threads.sync(() -> sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Restart the Plugin to effect the changes regarding the Database.")));
        });
    }

    @Console
    @CommandPermissions({ "projectretile.general.debug" })
    @Command(aliases = { "debug" }, desc = "Enables or disables debug mode", usage = "debug", max = 0)
    public static void onSubDebug(CommandSender sender, CommandContext args) throws CommandException {
        sender.sendMessage(formatLegacy(RED + "> " + GRAY + "ProjectRetile is using the " + DARK_AQUA + "Slf4j " + GRAY + "logger."));
        sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Enabling the debug mode is not possible using just a command."));
        sender.sendMessage(formatLegacy(RED + "> " + GRAY + "Please visit the wiki: " + DARK_AQUA + "https://github.com/Acquized/ProjectRetile/wiki/enabling-debug"));
        ProjectRetile.getInstance().getLog().debug("Player " + sender.getName() + " issued the debug command, but it already has been enabled.");
    }

    @Console
    @CommandPermissions({ "projectretile.general.dump" })
    @Command(aliases = { "dump" }, desc = "Creates a system dump useful for plugin debugging", usage = "dump", max = 0)
    public static void onSubDump(CommandSender sender, CommandContext args) throws CommandException {
        Threads.async(() -> {
            try {
                URL url = new URL("http://hastebin.com/documents");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "text/plain");
                conn.setRequestMethod("POST");
                conn.setUseCaches(false);
                conn.setDoOutput(true);

                OutputStream out = conn.getOutputStream();
                out.write(ProjectRetile.getInstance().getGson().toJson(Dump.create()).getBytes("UTF-8"));
                out.close();

                JsonObject obj = ProjectRetile.getInstance().getJsonParser().parse(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
                String key = obj.get("key").getAsString();
                Threads.sync(() -> sender.sendMessage(formatLegacy(RED + "> " + GRAY + "A dump has been successfully created. View it at " + DARK_AQUA + "http://hastebin.com/{0}", key)));
            } catch (IOException | RetileAPIException ex) {
                ProjectRetile.getInstance().getLog().error("Could not create dump.", ex);
                Threads.sync(() -> sender.sendMessage(formatLegacy(RED + "> " + GRAY + "A error occured. Please checkout the console.")));
            }
        });
    }

    public static class Parent {

        @Console
        @CommandPermissions({ "projectretile.general" })
        @NestedCommand(value = RetileCommand.class, executeBody = true)
        @Command(aliases = { "retile" }, desc = "Main command for ProjectRetile used for administration of the plugin",
                 usage = "[Debug, Reload, Dump]", max = 1)
        public static void onParent(CommandSender sender, CommandContext args) throws CommandException {
            if(args.argsLength() == 0) {
                sender.sendMessage(formatLegacy(RED + "> " + GRAY + "ProjectRetile v" + ProjectRetile.getInstance().getDescription().getVersion()));
                sender.sendMessage(formatLegacy(RED + "> " + GRAY + "GitHub: " + DARK_AQUA + "https://github.com/Acquized/ProjectRetile"));
            }
        }

    }

}
