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

import com.blackypaw.simpleconfig.SimpleConfig;
import com.blackypaw.simpleconfig.annotation.Comment;

public class Config extends SimpleConfig {

    @Comment("Put in here the Prefix that should be infront of every Message")
    public String prefix = "&c> &7";

    @Comment("Put in here a Language Code in which the Messages should be printed. \n" +
            "For this to work, there needs to be a messages_<locale>.properties File in the locale Folder.")
    public String locale = "en";

    @Comment("Put in here your prefered UUID Conversation Service. \n" +
             "If you don't know what this is, just leave it blank. \n" +
             "Bad things will happen if you don't know what you're doing! \n" +
             "Visit the Wiki for a list of Conversation Services.")
    public String conversationService = "MCAPI/CANADA/539439";

    @Comment("Should Messages be clickable? This will automaticly execute the preffered Command.")
    public boolean clickableMessages = true;

    @Comment("Don't change this value unless you want to reset the Config.")
    public String version = "1.0.0-SNAPSHOT";

}
