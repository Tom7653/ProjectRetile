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

import com.github.acquized.retile.ProjectRetile;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

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
                        try {
                            ProjectRetile.getInstance().getConfig().reload();
                        } catch (InvalidConfigurationException ex) {
                            sender.sendMessage(formatLegacy(RED + "> " + GRAY + "A error occured. Please check the Console."));
                            ProjectRetile.getInstance().getLog().error("Could not reload Config. Please check for Errors.", ex);
                            return;
                        }
                        sender.sendMessage(formatLegacy(RED + "> " + GRAY + "The Config has been successfully reloaded."));

                        ProjectRetile.getInstance().getI18n().load();
                        sender.sendMessage(formatLegacy(RED + "> " + GRAY + "The Message File has been successfully reloaded."));
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
