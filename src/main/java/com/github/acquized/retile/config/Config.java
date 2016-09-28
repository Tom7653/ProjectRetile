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

public class Config extends YamlConfig {

    @Comment("Set here the Prefix that will be infront of every Messages")
    @Path("ProjectRetile.Prefix")
    public String prefix = "&c> &7";

    @Comments({
            "Set here the Language Code that should be used for Message Printing",
            "This requires a File named \"messages_<locale>.properties\" in the \"locale\" Directory.",
            "If no file with the Locale's Paramter exist, a error will be thrown."
    })
    @Path("ProjectRetile.Locale")
    public String locale = "en";

    @Comment("Should the Updater be enabled? The Updater checks every Hour for a new Version.")
    @Path("ProjectRetile.Updater")
    public boolean updater = true;

    @Comment("Don't change this value unless you want to reset the Config")
    @Path("ProjectRetile.Version")
    public String version = "1.0.0-SNAPSHOT";

    @Comments({
            "Should UUID and SQL Requests be sent async?",
            "This eliminates Server lag but may cause delay between the Plugin's Actions."
    })
    @Path("Connections.AsyncRequests")
    public boolean forceAsyncRequests = true;

    @Comments({
            "Is Connection Pooling allowed?",
            "This eliminates SQL <-> Server Delay but may need cause high Bandwidth",
            "Disable if noticeable Ping Lags occur while proceeding Reports. Recommended to keep on true."
    })
    @Path("Connections.AllowPools")
    public boolean allowConnectionPools = true;

    // -------------------------------------------------

    public Config(File file) {
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
