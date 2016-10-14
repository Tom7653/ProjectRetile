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
import com.github.acquized.retile.reports.Report;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.acquized.retile.i18n.I18n.tl;

public class InfoCommand extends Command {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(ProjectRetile.getInstance().getConfig().dateFormat);

    public InfoCommand() {
        super("reportinfo", null, ProjectRetile.getInstance().getConfig().infoAliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender.hasPermission("projectretile.commands.reportinfo")) {
            if(args.length == 1) {
                Report report;
                try {
                    report = ProjectRetile.getInstance().getApi().getReportsUsingToken(args[0]);
                } catch (RetileAPIException ex) {
                    sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.Error"));
                    return;
                }
                if(report != null) {
                    sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.HeaderFooter"));
                    sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.Format",
                            report.getToken(),
                            ProjectRetile.getInstance().getCache().username(report.getReporter()),
                            ProjectRetile.getInstance().getCache().username(report.getVictim()),
                            report.getReason(),
                            DATE_FORMAT.format(new Date(report.getTimestamp()))));
                    sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.HeaderFooter"));
                } else {
                    sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.Unknown"));
                    return;
                }
            }
        } else {
            sender.sendMessage(tl("ProjectRetile.General.NoPermission"));
        }
        sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.Syntax"));
    }

}
