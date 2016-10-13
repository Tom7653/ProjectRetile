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
package com.github.acquized.retile.libs.bungeeutils.BungeeUtil.configuration;

import com.github.acquized.retile.ProjectRetile;
import com.github.acquized.retile.annotations.Beta;
import com.github.acquized.retile.annotations.Documented;

import dev.wolveringer.BungeeUtil.AsyncCatcher;
import dev.wolveringer.BungeeUtil.HandleErrorAction;
import dev.wolveringer.configuration.file.YamlConfiguration;

/*
 * Replacement for Configuration of normal BungeeUtil
 */
@Beta
@Documented
public class Configuration {

    private static boolean timingsActive = true;
    private static String latestVersion = "ProjectRetile 1.0.0-SNAPSHOT (shading BungeeUtil 1.6.7.15)";

    public static YamlConfiguration getConfig() {
        return null;
    }

    public static void init() {
        ProjectRetile.getInstance().getLog().info("BungeeUtil Config replaced.");
    }

    public static String getByteBuffType() {
        return "direct";
    }

    public static boolean ramStatistics() {
        return true;
    }

    public static boolean isTerminalColored() {
        return false;
    }

    public static boolean isTimingsActive() {
        return timingsActive;
    }

    public static int getLoadingBufferSize() {
        return 65536;
    }

    public static boolean isFastBoot() {
        return false;
    }

    public static void setTimingsActive(boolean enabled) {
        timingsActive = enabled;
    }

    public static boolean isUpdaterActive() {
        return false;
    }

    public static String getLastVersion() {
        return "ProjectRetile 1.0.0-SNAPSHOT (shading BungeUtil 1.6.7.15)";
    }

    public static void setLastVersion(String oldVerstion) {
        latestVersion = oldVerstion;
    }

    public static AsyncCatcher.AsyncCatcherMode getAsyncMode() {
        return AsyncCatcher.AsyncCatcherMode.WARNING;
    }

    public static boolean isGCEnabled() {
        return false;
    }

    public static boolean isDebugEnabled() {
        return false;
    }

    public static boolean isSyncInventoryClickActive() {
        return true;
    }

    public static boolean isScoreboardhandleEnabled() {
        return false;
    }

    public static boolean isBossBarhandleEnabled() {
        return false;
    }

    public static HandleErrorAction getHandleExceptionAction() {
        return HandleErrorAction.DISCONNECT;
    }

}
