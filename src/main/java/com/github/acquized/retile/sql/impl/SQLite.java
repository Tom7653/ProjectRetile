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
package com.github.acquized.retile.sql.impl;

import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.sql.Database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SQLite implements Database {

    private static Connection connection; // Connection Pooling for SQLite doesn't makes really sense

    private final String url;

    @Override
    public void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC").newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ProjectRetile.getInstance().getLog().error("Could not load SQLite Driver. Please download Driver and put in 'lib' Folder.", ex);
            return;
        }
        connection = DriverManager.getConnection(MessageFormat.format(url,
                ProjectRetile.getInstance().getDataFolder().getPath(), File.separator) + (url.endsWith(".db") ? "" : ".db"));
    }

    @Override
    public void disconnect() throws SQLException {
        if((connection != null) && (!connection.isClosed())) {
            connection.close();
        }
    }

    @Override
    public boolean isConnected() throws SQLException {
        try {
            return (connection != null) && (!connection.isClosed());
        } catch (SQLException ignored) {}
        return false;
    }

    @Override
    public boolean doesTableExist(String name) throws SQLException {
        try(ResultSet rs = connection.getMetaData().getTables(null, null, name, null)) {
            while(rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if((tName != null) && (tName.equals(name))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setup() throws SQLException {
        update("CREATE TABLE IF NOT EXISTS `retile` (token VARCHAR(12), reporter VARCHAR(64), victim VARCHAR(64), reason VARCHAR(128), reportdate BIGINT);");
        update("CREATE TABLE IF NOT EXISTS `queue` (token VARCHAR(12), reporter VARCHAR(64), victim VARCHAR(64), reason VARCHAR(128), reportdate BIGINT);");
        if(!doesTableExist("version")) {
            update("CREATE TABLE IF NOT EXISTS `version` (ver VARCHAR(64));");
            update("INSERT INTO `version` (ver) VALUES ('" + ProjectRetile.getInstance().getDescription().getVersion() + "');");
        } else {
            ResultSet rs = query("SELECT * FROM `version` LIMIT 1");
            while(rs.next()) {
                if(!rs.getString("ver").equals(ProjectRetile.getInstance().getDescription().getVersion())) {
                    ProjectRetile.getInstance().getLog().warn("ProjectRetile Database is outdated. Moving Tables to backup and resetting...");
                    update("RENAME TABLE `retile` TO `retileBackup`");
                    update("RENAME TABLE `queue` TO `queueBackup`");
                    update("RENAME TABLE `version` TO `versionBackup`");
                    setup();
                } else {
                    ProjectRetile.getInstance().getLog().info("ProjectRetile Database is up to date.");
                }
            }
        }
    }

    @Override
    public void update(String query) {
        PreparedStatement ps;
        try {
            ps = connection.prepareStatement(query);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ProjectRetile.getInstance().getLog().error("Could not execute SQL Update!", ex);
        }
    }

    @Override
    public ResultSet query(String query) {
        PreparedStatement ps;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(query);
            rs = ps.executeQuery();
        } catch (SQLException ex) {
            ProjectRetile.getInstance().getLog().error("Could not execute SQL Query!", ex);
        }
        return rs;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

}
