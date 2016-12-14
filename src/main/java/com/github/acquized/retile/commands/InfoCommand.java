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
import com.github.acquized.retile.reports.Report;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Console;

import net.md_5.bungee.api.CommandSender;

import java.util.Date;

import static com.github.acquized.retile.i18n.I18n.tl;

public class InfoCommand {

    @Console
    @CommandPermissions({ "projectretile.commands.reportinfo" })
    @Command(aliases = { "reportinfo", "inforeport", "info", "ir", "ri" }, usage = "<Token>",
             desc = "Shows advanced informations about a report", min = 1, max = 1)
    public static void onInfo(CommandSender sender, CommandContext args) throws CommandException {
        Report report;
        try {
            report = ProjectRetile.getInstance().getApi().getReportsUsingToken(args.getString(0));
        } catch (RetileAPIException ex) {
            throw new CommandException("A error occured when resolving a report using token \"" + args.getString(0) + "\".");
        }
        if(report != null) {
            sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.HeaderFooter"));
            sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.Format",
                    report.getToken(),
                    ProjectRetile.getInstance().getCache().username(report.getReporter()),
                    ProjectRetile.getInstance().getCache().username(report.getVictim()),
                    report.getReason(),
                    ProjectRetile.getInstance().getDateFormat().format(new Date(report.getTimestamp()))));
            sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.HeaderFooter"));
        } else {
            sender.sendMessage(tl("ProjectRetile.Commands.ReportInfo.Unknown"));
        }
    }

}
