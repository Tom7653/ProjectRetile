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
package com.github.acquized.retile.api;

import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.hub.Notifications;
import com.github.acquized.retile.reports.Report;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.acquized.retile.i18n.I18n.tl;

public class RetileAPIProvider implements RetileAPI {

    @Override
    public Report[] getLatestReports(int amount) throws RetileAPIException {
        List<Report> reports = new ArrayList<>();

        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `retile` LIMIT " + amount);
        try {
            while(rs.next()) {
                reports.add(new Report(rs.getString("token"), UUID.fromString(rs.getString("reporter")), UUID.fromString(rs.getString("victim")),
                        rs.getString("reason"), rs.getDate("reportdate").getTime()));
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Error while executing SQL Query", ex);
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
                        rs.getString("reason"), rs.getDate("reportdate").getTime()));
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Error while executing SQL Query", ex);
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
                        rs.getString("reason"), rs.getDate("reportdate").getTime()));
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Error while executing SQL Query", ex);
        }

        return reports.toArray(new Report[reports.size()]);
    }

    @Override
    public Report[] getReportsSince(long millis) throws RetileAPIException {
        List<Report> reports = new ArrayList<>();

        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `retile`");
        try {
            while(rs.next()) {
                if(rs.getDate("reportdate").getTime() >= millis) {
                    reports.add(new Report(rs.getString("token"), UUID.fromString(rs.getString("reporter")), UUID.fromString(rs.getString("victim")),
                            rs.getString("reason"), rs.getDate("reportdate").getTime()));
                }
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Error while executing SQL Query", ex);
        }

        return reports.toArray(new Report[reports.size()]);
    }

    @Override
    public void addReport(Report report) throws RetileAPIException {
        for(String s : ProjectRetile.getInstance().getBlacklist().list) {
            if((report.getReason().contains(s)) && (!ProxyServer.getInstance().getPlayer(report.getReporter()).hasPermission("projectretile.blacklist.bypass"))) {
                ProxyServer.getInstance().getPlayer(report.getReporter()).sendMessage(tl("ProjectRetile.Commands.Report.Blacklist"));
                return;
            }
        }

        List<ProxiedPlayer> staff = new ArrayList<>();

        String reporter = ProjectRetile.getInstance().getCache().username(report.getReporter());
        String victim = ProjectRetile.getInstance().getCache().username(report.getVictim());

        for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            if(Notifications.getInstance().isReceiving(ProjectRetile.getInstance().getCache().uuid(p.getName()))) {
                staff.add(p);
            }
        }

        if(staff.size() > 0) {
            for(ProxiedPlayer p : staff) {
                p.sendMessage(tl("ProjectRetile.Notifications.Report.Staff", reporter, victim, report.getReason(), resolveServer(report.getVictim()).getName()));
            }
        } else {
            ProjectRetile.getInstance().getDatabase().update("INSERT INTO `queue` (token, reporter, victim, reason, reportdate) VALUES " +
                    "('" + report.getToken() + "', '" + report.getReporter().toString() + "', '" + report.getVictim().toString() + "', '" + report.getReason() + "', NOW());");
        }

        ProxyServer.getInstance().getConsole().sendMessage(tl("ProjectRetile.Notifications.Report.Console",
                reporter, victim, report.getReason(), resolveServer(report.getVictim()), report.getToken()));

        ProjectRetile.getInstance().getDatabase().update("INSERT INTO `retile` (token, reporter, victim, reason, reportdate) VALUES " +
                "('" + report.getToken() + "', '" + report.getReporter().toString() + "', '" + report.getVictim().toString() + "', '" + report.getReason() + "', NOW());");
    }

    @Override
    @SuppressWarnings("LoopStatementThatDoesntLoop")
    public boolean doesReportExist(Report report) throws RetileAPIException {
        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `retile` WHERE token = '" + report.getToken() + "'");

        try {
            while(rs.next()) {
                return true;
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Error while executing SQL Query", ex);
        }

        return false;
    }

    @Override
    public void removeReport(Report report) throws RetileAPIException {
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
    public Report[] getReportsUsingReporter(UUID uuid) throws RetileAPIException {
        List<Report> reports = new ArrayList<>();

        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `retile` WHERE reporter = '" + uuid.toString() + "'");
        try {
            while(rs.next()) {
                reports.add(new Report(rs.getString("token"), UUID.fromString(rs.getString("reporter")), UUID.fromString(rs.getString("victim")),
                        rs.getString("reason"), rs.getDate("reportdate").getTime()));
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Error while executing SQL Query", ex);
        }

        return reports.toArray(new Report[reports.size()]);
    }

    @Override
    public Report[] getReportsUsingVictim(UUID uuid) throws RetileAPIException {
        List<Report> reports = new ArrayList<>();

        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `retile` WHERE victim = '" + uuid.toString() + "'");
        try {
            while(rs.next()) {
                reports.add(new Report(rs.getString("token"), UUID.fromString(rs.getString("reporter")), UUID.fromString(rs.getString("victim")),
                        rs.getString("reason"), rs.getDate("reportdate").getTime()));
            }
        } catch (SQLException ex) {
            throw new RetileAPIException("Error while executing SQL Query", ex);
        }

        return reports.toArray(new Report[reports.size()]);
    }

    @Override
    @SuppressWarnings("LoopStatementThatDoesntLoop")
    public Report getReportsUsingToken(String token) throws RetileAPIException {
        ResultSet rs = ProjectRetile.getInstance().getDatabase().query("SELECT * FROM `retile` WHERE token = '" + token + "'");
        try {
            while(rs.next()) {
                return new Report(rs.getString("token"), UUID.fromString(rs.getString("reporter")), UUID.fromString(rs.getString("victim")),
                        rs.getString("reason"), rs.getDate("reportdate").getTime());
            }
        } catch (SQLException | NullPointerException ex) {
            throw new RetileAPIException("Error while executing SQL Query", ex);
        }

        return null;
    }

    @Override
    public ServerInfo resolveServer(UUID uuid) throws RetileAPIException {
        return ProxyServer.getInstance().getPlayer(uuid).getServer().getInfo();
    }

}
