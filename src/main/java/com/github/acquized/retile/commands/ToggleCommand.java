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

import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.notifications.Notifications;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import static com.github.acquized.retile.i18n.I18n.tl;

public class ToggleCommand extends Command {

    @Inject
    public ToggleCommand(ProjectRetile retile) {
        super("togglereports", null, retile.config.toggleAliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if((p.hasPermission("projectretile.commands.togglereports")) && (p.hasPermission("projectretile.report.receive"))) {
                if(args.length == 0) {
                    if(Notifications.getInstance().isReceiving(p)) {
                        Notifications.getInstance().unsetReceiving(p);
                        p.sendMessage(tl("ProjectRetile.Commands.Toggle.On"));
                    } else {
                        Notifications.getInstance().setReceiving(p);
                        p.sendMessage(tl("ProjectRetile.Commands.Toggle.Off"));
                    }
                    return;
                }
            } else {
                p.sendMessage(tl("ProjectRetile.General.NoPermission"));
                return;
            }
        } else {
            sender.sendMessage(tl("ProjectRetile.General.PlayersPermitted"));
            return;
        }
        sender.sendMessage(tl("ProjectRetile.Commands.Toggle.Syntax"));
    }

}
