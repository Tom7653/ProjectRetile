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
import com.github.acquized.retile.api.RetileAPIException;
import com.github.acquized.retile.cooldown.CooldownManager;
import com.github.acquized.retile.reports.Report;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.acquized.retile.i18n.I18n.tl;

public class ReportCommand extends Command {

    public ReportCommand() {
        super("report", null, ProjectRetile.getInstance().getConfig().reportAliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            UUID pUUID = ProjectRetile.getInstance().getCache().uuid(p.getName());
            if(p.hasPermission("projectretile.commands.report")) {
                if(args.length >= 1) {
                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
                    if(target != null) {
                        UUID targetUUID = ProjectRetile.getInstance().getCache().uuid(target.getName());
                        if(!pUUID.toString().equals(targetUUID.toString())) {
                            if(!CooldownManager.getInstance().isInCooldown(p)) {
                                if(!target.hasPermission("projectretile.report.bypass")) {
                                    StringBuilder builder = new StringBuilder(args[1]);
                                    for(int i = 2; i < args.length; i++) {
                                        builder.append(" ").append(args[i]);
                                    }
                                    Report report = new Report(pUUID, targetUUID, builder.toString(), System.currentTimeMillis());
                                    try {
                                        ProjectRetile.getInstance().getApi().addReport(report);
                                    } catch (RetileAPIException ex) {
                                        if(!ex.getMessage().contains("Blacklist")) {
                                            p.sendMessage(tl("ProjectRetile.Commands.Report.Failure"));
                                        }
                                        return;
                                    }
                                    p.sendMessage(tl("ProjectRetile.Commands.Report.Success", target.getName(), report.getToken()));
                                    if(!p.hasPermission("projectretile.cooldown.bypass")) {
                                        CooldownManager.getInstance().addCooldown(p);
                                    }
                                    return;
                                } else {
                                    p.sendMessage(tl("ProjectRetile.Commands.Report.Bypass"));
                                    return;
                                }
                            } else {
                                p.sendMessage(tl("ProjectRetile.Commands.Report.Cooldown", CooldownManager.getInstance().getRemaining(p, TimeUnit.SECONDS)));
                                return;
                            }
                        } else {
                            p.sendMessage(tl("ProjectRetile.Commands.Report.ForeverAlone"));
                            return;
                        }
                    } else {
                        p.sendMessage(tl("ProjectRetile.Commands.Report.TargetUnknown"));
                        return;
                    }
                }
            } else {
                p.sendMessage(tl("ProjectRetile.General.NoPermission"));
                return;
            }
        } else {
            sender.sendMessage(tl("ProjectRetile.General.PlayersPermitted"));
            return;
        }
        sender.sendMessage(tl("ProjectRetile.Commands.Report.Syntax"));
    }

}
