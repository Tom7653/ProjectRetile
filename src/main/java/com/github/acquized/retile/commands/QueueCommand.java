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
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import dev.wolveringer.bungeeutil.inventory.Inventory;
import dev.wolveringer.bungeeutil.item.ItemStack;
import dev.wolveringer.bungeeutil.item.Material;
import dev.wolveringer.bungeeutil.item.meta.SkullMeta;
import dev.wolveringer.bungeeutil.packets.PacketPlayInWindowClick;
import dev.wolveringer.bungeeutil.player.Player;

import static com.github.acquized.retile.i18n.I18n.tl;
import static com.github.acquized.retile.utils.Utility.DARK_AQUA;
import static com.github.acquized.retile.utils.Utility.GRAY;
import static com.github.acquized.retile.utils.Utility.GREEN;
import static com.github.acquized.retile.utils.Utility.RED;
import static com.github.acquized.retile.utils.Utility.formatLegacy;

public class QueueCommand extends Command {

    private final SimpleDateFormat format;
    
    private ProjectRetile retile;
    private RetileAPI api;
    private Cache cache;

    @Inject
    public QueueCommand(ProjectRetile retile, RetileAPI api, Cache cache) {
        super("waitingqueue", null, retile.config.queueAliases);
        this.retile = retile;
        this.api = api;
        this.cache = cache;
        this.format = new SimpleDateFormat(retile.config.dateFormat);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(p.hasPermission("projectretile.commands.queue")) {
                if(args.length == 0) {
                    Report[] reports;
                    try {
                        reports = api.getWaitingReports();
                    } catch (RetileAPIException ex) {
                        p.sendMessage(tl("ProjectRetile.Commands.Queue.Error"));
                        return;
                    }
                    Inventory inv = new Inventory(54, "Reports in Queue (" + reports.length + ")");
                    Player player = (Player) p;
                    int slot = 0;
                    for(final Report r : reports) {
                        String reporter = cache.username(r.getReporter());
                        final String victim = cache.username(r.getVictim());
                        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3) {
                            @Override
                            public void click(Click click) {
                                try {
                                    if(click.getMode() == PacketPlayInWindowClick.Mode.NORMAL_LEFT_CLICK) {
                                        click.getPlayer().connect(api.resolveServer(r.getVictim()));
                                    } else if(click.getMode() == PacketPlayInWindowClick.Mode.NORMAL_RIGHT_CLICK) {
                                        api.removeReport(r);
                                        click.getPlayer().sendMessage(formatLegacy(RED + "> " + GRAY + "Report " + DARK_AQUA + r.getToken() + GRAY + " deleted."));
                                        click.getPlayer().closeInventory();
                                    }
                                } catch (RetileAPIException ex) {
                                    retile.getLog().error("Could not resolve Server of " + victim, ex);
                                }
                            }
                        };
                        SkullMeta meta = (SkullMeta) item.getItemMeta();
                        meta.setSkullOwner(victim);
                        meta.setDisplayName(DARK_AQUA + victim);
                        meta.setLore(Arrays.asList(GRAY + "ID: " + DARK_AQUA + r.getToken(),
                                GRAY + "Reported by: " + DARK_AQUA + reporter,
                                GRAY + "Reason: " + DARK_AQUA + r.getReason(),
                                GRAY + "Time: " + DARK_AQUA + format.format(new Date(r.getTimestamp())),
                                GRAY + " ",
                                GREEN + "Left click to connect",
                                RED + "Right click to delete"));
                        inv.setItem(slot, item);
                        slot++;
                    }
                    try {
                        api.clearWaitingQueue();
                    } catch (RetileAPIException ex) {
                        p.sendMessage(tl("ProjectRetile.Commands.Queue.Error"));
                        return;
                    }
                    player.openInventory(inv);
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
        sender.sendMessage(tl("ProjectRetile.Commands.Queue.Syntax"));
    }

}
