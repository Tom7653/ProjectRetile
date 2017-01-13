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
package com.github.acquized.retile.api;

import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.notifications.Notifications;
import com.github.acquized.retile.reports.Report;
import com.github.acquized.retile.sql.Database;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.xerces.impl.dv.DatatypeValidator;

import lombok.NonNull;

import static com.github.acquized.retile.i18n.I18n.tl;
import static com.google.common.base.Preconditions.checkArgument;

public class RetileAPIProvider implements RetileAPI {

    @Override
    public Report[] getLatestReports(int amount) throws RetileAPIException {
        checkArgument(amount <= 0, "Amount is negative");

        List<Report> reports = new ArrayList<>();

        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `retile` LIMIT " + amount);
        try {
            while(rs.next()) {
                reports.add(new Report(rs.getString("token"), UUID.fromString(rs.getString("reporter")), UUID.fromString(rs.getString("victim")),
                        rs.getString("reason"), rs.getLong("reportdate")));
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Could not execute SQL query", ex);
        }

        return reports.toArray(new Report[reports.size()]);
    }

    @Override
    public Report[] getAllReports() throws RetileAPIException {
        List<Report> reports = new ArrayList<>();

        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `retile`");
        try {
            while(rs.next()) {
                reports.add(new Report(rs.getString("token"), UUID.fromString(rs.getString("reporter")), UUID.fromString(rs.getString("victim")),
                        rs.getString("reason"), rs.getLong("reportdate")));
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Could not execute SQL query", ex);
        }

        return reports.toArray(new Report[reports.size()]);
    }

    @Override
    public Report[] getWaitingReports() throws RetileAPIException {
        List<Report> reports = new ArrayList<>();

        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `queue`");
        try {
            while(rs.next()) {
                reports.add(new Report(rs.getString("token"), UUID.fromString(rs.getString("reporter")), UUID.fromString(rs.getString("victim")),
                        rs.getString("reason"), rs.getLong("reportdate")));
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Could not execute SQL query", ex);
        }

        return reports.toArray(new Report[reports.size()]);
    }

    @Override
    public Report[] getReportsSince(long millis) throws RetileAPIException {
        List<Report> reports = new ArrayList<>();

        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `retile`");
        try {
            while(rs.next()) {
                if(rs.getLong("reportdate") >= millis) {
                    reports.add(new Report(rs.getString("token"), UUID.fromString(rs.getString("reporter")), UUID.fromString(rs.getString("victim")),
                            rs.getString("reason"), rs.getLong("reportdate")));
                }
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Could not execute SQL query", ex);
        }

        return reports.toArray(new Report[reports.size()]);
    }

    @Deprecated
    public void addReport(@NonNull Report report) throws RetileAPIException {
        processReport(report);
    }

    @Override
    public void processReport(@NonNull Report report) throws RetileAPIException {
        for(Object obj : ProjectRetile.getInstance().getBlacklist().getList("blacklist")) {
            String s = (String) obj;
            if((report.getReason().contains(s)) && (!ProxyServer.getInstance().getPlayer(report.getReporter()).hasPermission("projectretile.blacklist.bypass"))) {
                ProxyServer.getInstance().getPlayer(report.getReporter()).sendMessage(tl("ProjectRetile.Commands.Report.Blacklist"));
                throw new RetileAPIException("Blacklist");
            }
        }

        List<ProxiedPlayer> staff = new ArrayList<>();

        String reporter = ProjectRetile.getInstance().getCache().username(report.getReporter());
        String victim = ProjectRetile.getInstance().getCache().username(report.getVictim());

        staff.addAll(ProxyServer.getInstance().getPlayers().stream().filter(p -> (p.hasPermission("projectretile.report.receive")) && (Notifications.getInstance().isReceiving(p))).collect(Collectors.toList()));

        Database database = ProjectRetile.getInstance().getDatabase();
        if(staff.size() > 0) {
            BaseComponent[] components = tl("ProjectRetile.Notifications.Report.Staff", reporter, victim, report.getReason(), resolveServer(report.getVictim()).getName());
            if(ProjectRetile.getInstance().getConfig().getBoolean("General.clickablemessages")) {
                for(BaseComponent c : components) {
                    c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + resolveServer(report.getVictim()).getName()));
                }
            }

            for(ProxiedPlayer p : staff) {
                p.sendMessage(components);
            }
        } else {
        	database.update("INSERT INTO `queue` (token, reporter, victim, reason, reportdate) VALUES " +
                    "('" + report.getToken() + "', '" + report.getReporter().toString() + "', '" + report.getVictim().toString()
                    + "', '" + database.encodeParameter(report.getReason()) + "', " + System.currentTimeMillis() + ");"); //Only the reason need to be encoded. All other its generated by the system
        }

        ProxyServer.getInstance().getConsole().sendMessage(tl("ProjectRetile.Notifications.Report.Console",
                reporter, victim, report.getReason(), resolveServer(report.getVictim()).getName(), report.getToken()));

        database.update("INSERT INTO `retile` (token, reporter, victim, reason, reportdate) VALUES " +
                "('" + report.getToken() + "', '" + report.getReporter().toString() + "', '" + report.getVictim().toString() + "', '" + database.encodeParameter(report.getReason()) + "', " + System.currentTimeMillis() + ");");
    }

    @Override
    @SuppressWarnings("LoopStatementThatDoesntLoop")
    public boolean doesReportExist(@NonNull Report report) throws RetileAPIException {
        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `retile` WHERE token = '" + report.getToken() + "'");

        try {
            while(rs.next()) {
                return true;
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Could not execute SQL query", ex);
        }

        return false;
    }

    @Override
    public void removeReport(@NonNull Report report) throws RetileAPIException {
        if(doesReportExist(report)) {
            ProjectRetile.getInstance().getDatabase().update("DELETE FROM `retile` WHERE token = '" + report.getToken() + "'");
        }
    }

    @Override
    public void clearWaitingQueue() throws RetileAPIException {
        ProjectRetile.getInstance().getDatabase().update("DELETE FROM queue;");
        ProjectRetile.getInstance().getDatabase().update("ALTER TABLE queue AUTO_INCREMENT = 1");
    }

    @Override
    public Report[] getReportsUsingReporter(@NonNull UUID uuid) throws RetileAPIException {
        List<Report> reports = new ArrayList<>();

        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `retile` WHERE reporter = '" + uuid.toString() + "'");
        try {
            while(rs.next()) {
                reports.add(new Report(rs.getString("token"), UUID.fromString(rs.getString("reporter")), UUID.fromString(rs.getString("victim")),
                        rs.getString("reason"), rs.getLong("reportdate")));
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Could not execute SQL query", ex);
        }

        return reports.toArray(new Report[reports.size()]);
    }

    @Override
    public Report[] getReportsUsingVictim(@NonNull UUID uuid) throws RetileAPIException {
        List<Report> reports = new ArrayList<>();

        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `retile` WHERE victim = '" + uuid.toString() + "'");
        try {
            while(rs.next()) {
                reports.add(new Report(rs.getString("token"), UUID.fromString(rs.getString("reporter")), UUID.fromString(rs.getString("victim")),
                        rs.getString("reason"), rs.getLong("reportdate")));
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Could not execute SQL query", ex);
        }

        return reports.toArray(new Report[reports.size()]);
    }

    @Override
    @SuppressWarnings("LoopStatementThatDoesntLoop")
    public Report getReportsUsingToken(@NonNull String token) throws RetileAPIException {
        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `retile` WHERE token = '" + token + "'");
        try {
            while(rs.next()) {
                return new Report(rs.getString("token"), UUID.fromString(rs.getString("reporter")), UUID.fromString(rs.getString("victim")),
                        rs.getString("reason"), rs.getLong("reportdate"));
            }
        } catch (SQLException | NullPointerException ex) {
            throw new RetileAPIException("Could not execute SQL query", ex);
        }

        return null;
    }

    @Override
    public ServerInfo resolveServer(@NonNull UUID uuid) throws RetileAPIException {
        return ProxyServer.getInstance().getPlayer(uuid).getServer().getInfo();
    }

}
