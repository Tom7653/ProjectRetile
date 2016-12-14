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

import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.api.RetileAPIException;
import com.github.acquized.retile.cooldown.Cooldown;
import com.github.acquized.retile.reports.Report;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.acquized.retile.i18n.I18n.tl;

public class ReportCommand {

    @CommandPermissions({ "projectretile.commands.report" })
    @Command(aliases = { "report", "r", "ticket", "reportuser" }, usage = "<Player> <Reason ...>",
             desc = "Reports a player to the staff", min = 2)
    public static void onReport(CommandSender sender, CommandContext args) throws CommandException {
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args.getString(0));
        if(target != null) {
            UUID senderUUID = ProjectRetile.getInstance().getCache().uuid(sender.getName());
            UUID targetUUID = ProjectRetile.getInstance().getCache().uuid(target.getName());
            if(senderUUID.compareTo(targetUUID) != 0) {
                if(!Cooldown.getInstance().inCooldown(senderUUID)) {
                    if(!target.hasPermission("projectretile.commands.report.bypass")) {
                        Report report = new Report(senderUUID, targetUUID, args.getJoinedStrings(1), System.currentTimeMillis());
                        try {
                            ProjectRetile.getInstance().getApi().processReport(report);
                        } catch (RetileAPIException ex) {
                            if(!ex.getMessage().toLowerCase().contains("blacklist"))
                                sender.sendMessage(tl("ProjectRetile.Commands.Report.Failure"));
                            return;
                        }
                        sender.sendMessage(tl("ProjectRetile.Commands.Report.Success", target.getName(), report.getToken()));
                        if(!sender.hasPermission("projectretile.cooldown.bypass")) {
                            Cooldown.getInstance().start(senderUUID);
                        }
                    } else {
                        sender.sendMessage(tl("ProjectRetile.Commands.Report.Bypass"));
                    }
                } else {
                    sender.sendMessage(tl("ProjectRetile.Commands.Report.Cooldown", Cooldown.getInstance().getRemaining(senderUUID, TimeUnit.SECONDS)));
                }
            } else {
                sender.sendMessage(tl("ProjectRetile.Commands.Report.ForeverAlone"));
            }
        } else {
            sender.sendMessage(tl("ProjectRetile.Commands.Report.TargetUnknown"));
        }
    }

}
