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
package com.github.acquized.retile.config;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.ConfigMode;
import net.cubespace.Yamler.Config.Path;
import net.cubespace.Yamler.Config.YamlConfig;

import java.io.File;

public class DBConfig extends YamlConfig {

    @Comments({
            "Set here the URL of the Database to which should be connected",
            "Must be \"jdbc:<Database engine>:<connection parameter>\".",
            "Template for MySQL: jdbc:mysql://<host>:<port>/<database>"
    })
    @Path("URL")
    public String jdbcURL = "jdbc:mysql://127.0.0.1:3306/ProjectRetile";

    @Comment("Set here the Username that should be used to authentificate with the Database")
    @Path("Username")
    public String username = "root";

    @Comment("Set here the Password that should be used to authentificate with the Database")
    @Path("Password")
    public String password = "passw0rd";

    // -------------------------------------------------

    public DBConfig(File file) {
        CONFIG_FILE = file;
        CONFIG_HEADER = new String[]{
                "  ____            _           _   ____      _   _ _      ",
                " |  _ \\ _ __ ___ (_) ___  ___| |_|  _ \\ ___| |_(_) | ___ ",
                " | |_) | '__/ _ \\| |/ _ \\/ __| __| |_) / _ \\ __| | |/ _ \\",
                " |  __/| | | (_) | |  __/ (__| |_|  _ <  __/ |_| | |  __/",
                " |_|   |_|  \\___// |\\___|\\___|\\__|_| \\_\\___|\\__|_|_|\\___|",
                "               |__/                                      "};
        CONFIG_MODE = ConfigMode.DEFAULT;
    }

}
