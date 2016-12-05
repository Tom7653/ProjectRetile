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
package com.github.acquized.retile.database;

import lombok.Getter;

public enum Type {

    MYSQL("http://central.maven.org/maven2/mysql/mysql-connector-java/6.0.5/mysql-connector-java-6.0.5.jar", "MySQLDriver.jar", "com.mysql.jdbc.Driver"),
    SQLITE("http://central.maven.org/maven2/org/xerial/sqlite-jdbc/3.15.1/sqlite-jdbc-3.15.1.jar", "SQLiteDriver.jar", "org.sqlite.JDBC"),
    MARIADB("http://central.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/1.5.5/mariadb-java-client-1.5.5.jar", "MariaDBDriver.jar", "org.mariadb.jdbc.Driver"),
    H2("http://central.maven.org/maven2/com/h2database/h2/1.4.193/h2-1.4.193.jar", "H2Driver.jar", "org.h2.Driver");

    @Getter private String url;
    @Getter private String fileName;
    @Getter private String main;

    Type(String url, String fileName, String main) {
        this.url = url;
        this.fileName = fileName;
        this.main = main;
    }

}
