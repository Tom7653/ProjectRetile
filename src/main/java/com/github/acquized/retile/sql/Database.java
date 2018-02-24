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
package com.github.acquized.retile.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.Codec;
import org.owasp.esapi.codecs.OracleCodec;

public interface Database {
	static Codec SQL_CODEC = new OracleCodec();
	
    void connect() throws SQLException;
    void disconnect() throws SQLException;

    boolean isConnected() throws SQLException;
    boolean doesTableExist(String name) throws SQLException;

    void setup() throws SQLException;

    void update(String query);
    ResultSet query(String query);
    default String encodeParameter(String parm){
    	return ESAPI.encoder().encodeForSQL(SQL_CODEC, parm);
    }
    
    Connection getConnection() throws SQLException;

    /* TODO list for databases
     * - [✔] MySQL
     * - [✔] SQLite
     * - [✘] MariaDB
     * - [✘] H2 (may need restructuring)
     * - [?] Hive (may need restructuring)
     * - [✘] Redis (may need restructuring)
     *
     * Maybe I should download all drivers at runtime and load them dynamically (decreases file size)
     */

}
