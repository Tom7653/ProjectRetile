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

import net.md_5.bungee.api.CommandSender;

import java.util.Arrays;
import java.util.Date;

import dev.wolveringer.bungeeutil.inventory.Inventory;
import dev.wolveringer.bungeeutil.item.ItemStack;
import dev.wolveringer.bungeeutil.item.Material;
import dev.wolveringer.bungeeutil.item.meta.SkullMeta;
import dev.wolveringer.bungeeutil.packets.PacketPlayInWindowClick;
import dev.wolveringer.bungeeutil.player.Player;

import static com.github.acquized.retile.utils.Utility.DARK_AQUA;
import static com.github.acquized.retile.utils.Utility.GRAY;
import static com.github.acquized.retile.utils.Utility.GREEN;
import static com.github.acquized.retile.utils.Utility.RED;
import static com.github.acquized.retile.utils.Utility.formatLegacy;

public class ListReportsCommand {

    @CommandPermissions({ "projectretile.commands.listreports" })
    @Command(aliases = { "listreports", "reports", "lr" }, usage = "[Amount]",
             desc = "Shows a Gui with all or [Amount] submited reports", max = 1)
    public static void onListReports(CommandSender sender, CommandContext args) throws CommandException {
        int amount = 0;
        if (args.argsLength() == 1) {
            amount = args.getInteger(0);
        }
        Report[] reports;
        try {
            reports = (amount != 0 ? ProjectRetile.getInstance().getApi().getLatestReports(amount) : ProjectRetile.getInstance().getApi().getAllReports());
        } catch (RetileAPIException ex) {
            throw new CommandException("Could not connect to Retile API.");
        }
        Inventory inv = new Inventory(54, "Reports (" + reports.length + ")");
        Player p = (Player) sender;
        int slot = 0;
        for(final Report r : reports) {
            String reporter = ProjectRetile.getInstance().getCache().username(r.getReporter());
            final String victim = ProjectRetile.getInstance().getCache().username(r.getVictim());
            ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3) {
                @Override
                public void click(Click click) {
                    try {
                        if(click.getMode() == PacketPlayInWindowClick.Mode.NORMAL_LEFT_CLICK) {
                            click.getPlayer().connect(ProjectRetile.getInstance().getApi().resolveServer(r.getVictim()));
                        } else if(click.getMode() == PacketPlayInWindowClick.Mode.NORMAL_RIGHT_CLICK) {
                            ProjectRetile.getInstance().getApi().removeReport(r);
                            click.getPlayer().sendMessage(formatLegacy(RED + "> " + GRAY + "Report " + DARK_AQUA + r.getToken() + GRAY + " deleted."));
                            click.getPlayer().closeInventory();
                        }
                    } catch (RetileAPIException ex) {
                        ProjectRetile.getInstance().getLog().error("Could not resolve server of " + victim, ex);
                    }
                }
            };
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setSkullOwner(victim);
            meta.setDisplayName(DARK_AQUA + victim);
            meta.setLore(Arrays.asList(GRAY + "ID: " + DARK_AQUA + r.getToken(),
                    GRAY + "Reported by: " + DARK_AQUA + reporter,
                    GRAY + "Reason: " + DARK_AQUA + r.getReason(),
                    GRAY + "Time: " + DARK_AQUA + ProjectRetile.getInstance().getDateFormat().format(new Date(r.getTimestamp())),
                    GRAY + " ",
                    GREEN + "Left click to connect",
                    RED + "Right click to delete"));
            inv.setItem(slot, item);
            slot++;
        }
        p.openInventory(inv);
    }

}
