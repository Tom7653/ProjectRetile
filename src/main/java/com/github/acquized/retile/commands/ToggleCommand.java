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

import com.github.acquized.retile.notifications.Notifications;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import static com.github.acquized.retile.i18n.I18n.tl;

public class ToggleCommand {

    @CommandPermissions({ "projectretile.commands.togglereports", "projectretile.report.receive" })
    @Command(aliases = { "togglereports", "toggle", "tr" },
             desc = "Toggles receiving of incoming report messages", max = 0)
    public static void onToggle(CommandSender sender, CommandContext args) throws CommandException {
        if(Notifications.getInstance().isReceiving((ProxiedPlayer) sender)) {
            Notifications.getInstance().unsetReceiving((ProxiedPlayer) sender);
            sender.sendMessage(tl("ProjectRetile.Commands.Toggle.On"));
        } else {
            Notifications.getInstance().setReceiving((ProxiedPlayer) sender);
            sender.sendMessage(tl("ProjectRetile.Commands.Toggle.Off"));
        }
    }

}
