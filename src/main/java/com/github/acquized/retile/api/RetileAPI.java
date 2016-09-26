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

import com.github.acquized.retile.annotations.Beta;
import com.github.acquized.retile.annotations.Documented;
import com.github.acquized.retile.cache.Cache;
import com.github.acquized.retile.reports.Report;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 * TODO: JavaDocs
 */
@Beta
@Documented
public interface RetileAPI {

    /**
     *
     * @author Acquized
     * @return
     * @throws RetileAPIException
     */
    Report[] getLatestReports(int amount) throws RetileAPIException;

    /**
     *
     * @author Acquized
     * @return
     * @throws RetileAPIException
     */
    Report[] getAllReports() throws RetileAPIException;

    /**
     *
     * @author Acquized
     * @return
     * @throws RetileAPIException
     */
    Report[] getWaitingReports() throws RetileAPIException;

    /**
     *
     * @author Acquized
     * @param millis
     * @return
     * @throws RetileAPIException
     */
    Report[] getReportsSince(long millis) throws RetileAPIException;

    /**
     *
     * @author Acquized
     * @param report
     * @throws RetileAPIException
     */
    void addReport(Report report) throws RetileAPIException;

    /**
     *
     * @author Acquized
     * @param report
     * @return
     * @throws RetileAPIException
     */
    boolean doesReportExist(Report report) throws RetileAPIException;

    /**
     *
     * @author Acquized
     * @param report
     * @throws RetileAPIException
     */
    void removeReport(Report report) throws RetileAPIException;

    /**
     *
     * @author Acquized
     * @param uuid
     * @return
     * @throws RetileAPIException
     */
    Report[] getReportsBy(UUID uuid) throws RetileAPIException;

    /**
     *
     * @author Acquized
     * @param uuid
     * @return
     * @throws RetileAPIException
     */
    Report[] getReportsRegarding(UUID uuid) throws RetileAPIException;

    /**
     *
     * @author Acquized
     * @param uuid
     * @return
     * @throws RetileAPIException
     */
    String resolveServer(UUID uuid) throws RetileAPIException;

    /**
     *
     * @author Acquized
     * @return
     * @throws RetileAPIException
     * @throws SQLException
     * @deprecated
     */
    @Deprecated
    Connection getSQLConnection() throws RetileAPIException, SQLException;

    /**
     *
     * @author Acquized
     * @return
     * @throws RetileAPIException
     * @deprecated
     */
    @Deprecated
    Cache getCache() throws RetileAPIException;

}
