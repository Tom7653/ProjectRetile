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
package com.github.acquized.retile.listeners;

import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.api.RetileAPIException;
import com.github.acquized.retile.notifications.Notifications;
import com.github.acquized.retile.reports.Report;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

import static com.github.acquized.retile.i18n.I18n.tl;

public class PostLogin implements Listener {

    @EventHandler
    public void onPost(PostLoginEvent e) {
        ProxyServer.getInstance().getScheduler().schedule(ProjectRetile.getInstance(), () -> {
            if(e.getPlayer().hasPermission("projectretile.report.receive.offline")) {
                Report[] reports;
                try {
                    reports = ProjectRetile.getInstance().getApi().getWaitingReports();
                } catch (RetileAPIException ex) {
                    ProjectRetile.getInstance().getLog().error("Could not get Waiting Queue Reports.", ex);
                    return;
                }
                if(reports.length != 0) {
                    e.getPlayer().sendMessage(tl("ProjectRetile.Notifications.Report.Offline.Info"));
                }
            }
        }, 2, TimeUnit.SECONDS); // Delayed because of UUID Delays

        if(e.getPlayer().hasPermission("projectretile.report.receive")) {
            Notifications.getInstance().setReceiving(e.getPlayer());
        }
    }

}
