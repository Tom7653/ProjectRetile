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
package com.github.acquized.retile.utils;

import com.github.acquized.retile.ProjectRetile;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

import java.text.MessageFormat;
import java.util.logging.Handler;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Utility {

    public static final ChatColor BLACK = ChatColor.BLACK;
    public static final ChatColor DARK_BLUE = ChatColor.DARK_BLUE;
    public static final ChatColor DARK_GREEN = ChatColor.DARK_GREEN;
    public static final ChatColor DARK_AQUA = ChatColor.DARK_AQUA;
    public static final ChatColor DARK_RED = ChatColor.DARK_RED;
    public static final ChatColor DARK_PURPLE = ChatColor.DARK_PURPLE;
    public static final ChatColor GOLD = ChatColor.GOLD;
    public static final ChatColor GRAY = ChatColor.GRAY;
    public static final ChatColor DARK_GRAY = ChatColor.DARK_GRAY;
    public static final ChatColor BLUE = ChatColor.BLUE;
    public static final ChatColor GREEN = ChatColor.GREEN;
    public static final ChatColor AQUA = ChatColor.AQUA;
    public static final ChatColor RED = ChatColor.RED;
    public static final ChatColor LIGHT_PURPLE = ChatColor.LIGHT_PURPLE;
    public static final ChatColor YELLOW = ChatColor.YELLOW;
    public static final ChatColor WHITE = ChatColor.WHITE;
    public static final ChatColor MAGIC = ChatColor.MAGIC;
    public static final ChatColor BOLD = ChatColor.BOLD;
    public static final ChatColor STRIKETHROUGH = ChatColor.STRIKETHROUGH;
    public static final ChatColor UNDERLINE = ChatColor.UNDERLINE;
    public static final ChatColor ITALIC = ChatColor.ITALIC;
    public static final ChatColor RESET = ChatColor.RESET;

    public static String format(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static String format(String msg, Object... args) {
        return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(msg, args));
    }

    public static BaseComponent[] formatLegacy(String msg) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public static BaseComponent[] formatLegacy(String msg, Object... args) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', MessageFormat.format(msg, args)));
    }

    @SuppressWarnings("deprecation")
    public static void disablePlugin(Plugin p) {
        ProxyServer.getInstance().getScheduler().runAsync(ProjectRetile.getInjector().getInstance(ProjectRetile.class), () -> {
            try {
                p.onDisable();
                for(Handler h : p.getLogger().getHandlers()) {
                    h.close();
                }
                ProxyServer.getInstance().getScheduler().cancel(p);
                p.getExecutorService().shutdownNow();
            } catch (Exception ex) {
                ProjectRetile.getInjector().getInstance(ProjectRetile.class).getLog().error("A exception occured while disabling " + p.getDescription().getName() + ".", ex);
            }
        });
    }

    public static void disablePlugin(String name) {
        disablePlugin(ProxyServer.getInstance().getPluginManager().getPlugin(name));
    }

}