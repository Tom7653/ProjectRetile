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
package com.github.acquized.retile.i18n;

import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.utils.Utility;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class I18n {

    public static final String[] SUPPORTED_LOCALES = { "en", "de", /*"nl", "es", "fr" These Locales are outdated. */ };
    public static final File DIRECTORY = new File(ProjectRetile.getInstance().getDataFolder() + File.separator + "locale");

    public static ResourceBundle bundle;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public I18n() {
        try {
            if (!DIRECTORY.isDirectory()) {
                DIRECTORY.mkdirs();
            }
            for (String l : SUPPORTED_LOCALES) {
                File f = new File(DIRECTORY, "messages_" + l + ".properties");
                if (!f.exists()) {
                    Files.copy(ProjectRetile.getInstance().getResourceAsStream(f.getName()), f.toPath());
                }
            }
        } catch (IOException ex) {
            ProjectRetile.getInstance().getLog().error("Could not create Messages Files.", ex);
        }
    }

    public static String getMessage(String key) {
        return Utility.format(ProjectRetile.getInstance().getConfig().prefix + bundle.getString(key));
    }

    public static String getMessage(String key, Object... obj) {
        return Utility.format(ProjectRetile.getInstance().getConfig().prefix + bundle.getString(key), obj);
    }


    // Shortcuts for Static Imports

    public static BaseComponent[] tl(String key) {
        String msg = getMessage(key);
        if(msg.contains("\\n")) {
            String[] split = msg.split(Pattern.quote("\\n"));
            BaseComponent[] array = new BaseComponent[split.length];
            for(int i = 0; i < split.length; i++) {
                array[i] = new TextComponent(TextComponent.fromLegacyText(split[i]));
            }
            return array;
        } else {
            return TextComponent.fromLegacyText(msg);
        }
    }

    public static BaseComponent[] tl(String key, Object... obj) {
        String msg = getMessage(key, obj);
        if(msg.contains("\\n")) {
            String[] split = msg.split(Pattern.quote("\\n"));
            BaseComponent[] array = new BaseComponent[split.length];
            for(int i = 0; i < split.length; i++) {
                array[i] = new TextComponent(TextComponent.fromLegacyText(split[i]));
            }
            return array;
        } else {
            return TextComponent.fromLegacyText(msg);
        }
    }

    public void load() {
        try {
            ClassLoader loader = new URLClassLoader(new URL[]{DIRECTORY.toURI().toURL()});
            bundle = ResourceBundle.getBundle("messages", new Locale(ProjectRetile.getInstance().getConfig().locale), loader);
        } catch (IOException ex) {
            ProjectRetile.getInstance().getLog().error("Could not load messages_" + ProjectRetile.getInstance().getConfig().locale + ".properties File. " +
                    "Please check for Errors.", ex);
        }
    }

}
