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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MySQL implements Database {

    private static HikariDataSource dataSource; // only static because lombok

    private final String url;
    private final String username;
    private final char[] password;

    @Override
    public void connect() throws SQLException {
        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName("com.mysql.jdbc.Driver");
        cfg.setJdbcUrl(url);
        cfg.setUsername(username);
        cfg.setPassword(new String(password));
        cfg.setMinimumIdle(5);
        cfg.setMaximumPoolSize(100);
        cfg.setConnectionTimeout(3000);
        dataSource = new HikariDataSource(cfg);
    }

    @Override
    public void disconnect() throws SQLException {
        if((dataSource != null) && (!dataSource.isClosed())) {
            dataSource.close();
        }
    }

    @Override
    public void setup() throws SQLException {
        update("CREATE TABLE IF NOT EXISTS `retile` (token VARCHAR(12), reporter VARCHAR(64), victim VARCHAR(64), reason VARCHAR(128), reportdate DATETIME);");
        update("CREATE TABLE IF NOT EXISTS `queue` (token VARCHAR(12), reporter VARCHAR(64), victim VARCHAR(64), reason VARCHAR(128), reportdate DATETIME);");
    }

    @Override
    public void update(String query) {
        PreparedStatement ps;
        try {
            ps = getConnection().prepareStatement(query);
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
            ps = getConnection().prepareStatement(query);
            rs = ps.executeQuery();
        } catch (SQLException ex) {
            ProjectRetile.getInstance().getLog().error("Could not execute SQL Query!", ex);
        }
        return rs;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
