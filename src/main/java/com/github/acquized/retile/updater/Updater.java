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
package com.github.acquized.retile.updater;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.github.acquized.retile.ProjectRetile;

import net.md_5.bungee.api.ProxyServer;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

public class Updater {

    private static final String URL = "http://api.spiget.org/v2/resources/";
    private static final int PLUGIN = 11364; // TODO: Update as soon Retile has its own Page
    private static final String SUBURL = "/versions/latest";

    public static void start() {
        ProxyServer.getInstance().getScheduler().schedule(ProjectRetile.getInjector().getInstance(ProjectRetile.class), () -> {
            String updateMsg = getUpdateMessage();
            if(updateMsg != null) {
                ProjectRetile.getInjector().getInstance(ProjectRetile.class).getLog().info(updateMsg);
            }
        }, 0, 1, TimeUnit.HOURS);
    }

    public static String getUpdateMessage() {
        Version current = new Version(ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDescription().getVersion());
        Version newest = new Version(ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDescription().getVersion() + "-OFFLINE");
        try { newest = getNewestVersion().get(); } catch (InterruptedException | ExecutionException ignored) {}

        if(current.compareTo(newest) < 0) {
            return "There is a new Version available: " + newest.toString() + " (You are running " + current.toString() + ")";
        } else if(current.compareTo(newest) != 0) {
            if((current.getTag().toUpperCase().startsWith("DEV")) || (current.getTag().toUpperCase().startsWith("SNAPSHOT")) ||
               (current.getTag().toUpperCase().startsWith("PRE")) || (current.getTag().toUpperCase().startsWith("PRERELEASE"))) {
                return "You are running a developement Version of ProjectRetile! Please report any Bugs to our GitHub Page.";
            } else if(current.getTag().equalsIgnoreCase("OFFLINE")) {
                return "Could not check for Updates. Please check your Internet Connection.";
            } else {
                return "You are running a newer Version than released!";
            }
        }

        return null;
    }

    public static Future<Version> getNewestVersion() throws IllegalArgumentException {
        FutureTask<Version> task = new FutureTask<>(() -> {
            URL url = new URL(URL + PLUGIN + SUBURL + "?" + System.currentTimeMillis());
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.addRequestProperty("User-Agent", "ProjectRetile v" + ProjectRetile.getInjector().getInstance(ProjectRetile.class).getDescription().getVersion());
            conn.setRequestMethod("GET");
            conn.setUseCaches(true);
            conn.setDoOutput(true);

            JsonObject obj = Json.parse(new InputStreamReader(conn.getInputStream())).asObject();
            return new Version(obj.get("name").asString());
        });
        ProxyServer.getInstance().getScheduler().runAsync(ProjectRetile.getInjector().getInstance(ProjectRetile.class), task);
        return task;
    }

}
