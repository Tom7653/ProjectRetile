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
import com.github.acquized.retile.api.RetileAPI;
import com.github.acquized.retile.api.RetileAPIException;
import com.github.acquized.retile.cache.Cache;
import com.github.acquized.retile.reports.Report;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.acquized.retile.i18n.I18n.tl;

public class InfoCommand extends Command {

    private final SimpleDateFormat format;
    
    private ProjectRetile retile;
    private RetileAPI api;
    private Cache cache;

    @Inject
    public InfoCommand(ProjectRetile retile, RetileAPI api, Cache cache) {
        super("reportinfo", null, retile.config.infoAliases);
        this.retile = retile;
        this.api = api;
        this.cache = cache;
        this.format = new SimpleDateFormat(retile.config.dateFormat);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender.hasPermission("projectretile.commands.reportinfo")) {
            if(args.length == 1) {
                Report report;
                try {
                    report = api.getReportsUsingToken(args[0]);
                } catch (RetileAPIException ex) {
                    sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.Error"));
                    return;
                }
                if(report != null) {
                    sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.HeaderFooter"));
                    sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.Format",
                            report.getToken(),
                            cache.username(report.getReporter()),
                            cache.username(report.getVictim()),
                            report.getReason(),
                            format.format(new Date(report.getTimestamp()))));
                    sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.HeaderFooter"));
                    return;
                } else {
                    sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.Unknown"));
                    return;
                }
            }
        } else {
            sender.sendMessage(tl("ProjectRetile.General.NoPermission"));
            return;
        }
        sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.Syntax"));
    }

}
