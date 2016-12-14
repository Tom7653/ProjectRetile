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

import com.github.acquized.retile.annotations.Documented;
import com.github.acquized.retile.reports.Report;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

import lombok.NonNull;

/**
 * Interface for implementation of the RetileAPI. This has
 * been made a Interface for future API Versions that may
 * use other Types of Databases and not SQL Databases.
 *
 * @version 1.0.0
 * @author Acquized
 */
@Documented
public interface RetileAPI {

    /**
     * Gets the <code>amount</code> latest Reports that have been
     * put into the <code>retile</code> Database Table. This excludes
     * Waiting Queue Reports.
     *
     * @return Array of Reports with the length of <code>amount</code>
     * @param amount Integer
     * @throws RetileAPIException If an error occurs
     */
    Report[] getLatestReports(int amount) throws RetileAPIException;

    /**
     * Gets all Reports that have ever been submited and added
     * into the <code>retile</code> Database Table. This includes
     * Waiting Queue Reports.
     *
     * @return Array of Reports
     * @throws RetileAPIException If an error occurs
     */
    Report[] getAllReports() throws RetileAPIException;

    /**
     * Gets all Reports that have been submited and added
     * into the <code>queue</code> Database Table. This only
     * includes Waiting Queue Reports.
     *
     * @return Array of Reports
     * @throws RetileAPIException If an error occurs
     */
    Report[] getWaitingReports() throws RetileAPIException;

    /**
     * Gets all Reports that have been submited since <code>millis</code>
     * from the <code>retile</code> Databe Table. This includes
     * Waiting Queue Reports.
     *
     * @param millis Long (Milliseconds since UNIX Epoch Time)
     * @return Array of Reports
     * @throws RetileAPIException If an error occurs
     */
    Report[] getReportsSince(long millis) throws RetileAPIException;

    /**
     * Adds a Report into the <code>retile</code> Database Table and (if online)
     * notifies Staff Members and the console about it. This method will be called
     * when a player sends a Report using the Command or Gui. If no staff member is
     * online, the Report will be also added to the <code>queue</code> Database Table.
     *
     * @param report Report
     * @deprecated Use RetileAPI#processReport
     * @throws RetileAPIException If an error occurs
     */
    @Deprecated void addReport(@NonNull Report report) throws RetileAPIException;

    /**
     * Adds a Report into the <code>retile</code> Database Table and (if online)
     * notifies Staff Members and the console about it. This method will be called
     * when a player sends a Report using the Command or Gui. If no staff member is
     * online, the Report will be also added to the <code>queue</code> Database Table.
     *
     * @param report Report
     * @throws RetileAPIException If an error occurs
     */
    void processReport(@NonNull Report report) throws RetileAPIException;

    /**
     * Checks if a Report with the {@link com.github.acquized.retile.reports.Report#token} already
     * exists in the <code>retile</code> Database. This excludes the Waiting Queue.
     *
     * @param report Report
     * @return Boolean
     * @throws RetileAPIException If an error occurs
     */
    boolean doesReportExist(@NonNull Report report) throws RetileAPIException;

    /**
     * Removes every Report with the {@link com.github.acquized.retile.reports.Report#token} from
     * the <code>retile</code> and <code>queue</code> Database.
     *
     * @param report Report
     * @throws RetileAPIException If an error occurs
     */
    void removeReport(@NonNull Report report) throws RetileAPIException;

    /**
     * Removes every Report from the <code>queue</code> Database.
     *
     * @throws RetileAPIException If an error occurs
     */
    void clearWaitingQueue() throws RetileAPIException;

    /**
     * Gets every Report submited by <code>uuid</code> from the <code>retile</code>
     * and <code>queue</code> Table.
     *
     * @param uuid UUID
     * @return Array of Reports
     * @throws RetileAPIException If an error occurs
     */
    Report[] getReportsUsingReporter(@NonNull UUID uuid) throws RetileAPIException;

    /**
     * Gets every Report that have <code>uuid</code> as Victim from the <code>retile</code>
     * and <code>queue</code> Table.
     *
     * @param uuid UUID
     * @return Array of Reports
     * @throws RetileAPIException If an error occurs
     */
    Report[] getReportsUsingVictim(@NonNull UUID uuid) throws RetileAPIException;

    /**
     * Gets every Report that have <code>token</code> as Unique Token from the <code>retile</code>
     * and <code>queue</code> Table.
     *
     * @param token Unique Token Identifier
     * @return Report
     * @throws RetileAPIException If an error occurs
     */
    Report getReportsUsingToken(@NonNull String token) throws RetileAPIException;

    /**
     * Resolves the Server of <code>uuid</code>. This is a shortcut for
     * {@link net.md_5.bungee.api.ProxyServer#getServerInfo(String)}.
     *
     * @param uuid UUID
     * @return ServerInfo
     * @throws RetileAPIException If an error occurs
     */
    ServerInfo resolveServer(@NonNull UUID uuid) throws RetileAPIException;

}
