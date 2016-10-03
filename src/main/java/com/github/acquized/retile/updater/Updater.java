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

import org.asynchttpclient.Response;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Updater {

    private static final String URL = "http://api.spiget.org/v2/resources/";
    private static final int PLUGIN = 11364; // TODO: Update as soon Retile has its own Page
    private static final String SUBURL = "/versions/latest";

    public static void start() {
        ProxyServer.getInstance().getScheduler().schedule(ProjectRetile.getInstance(), new Runnable() {
            @Override
            public void run() {
                ProxyServer.getInstance().getScheduler().runAsync(ProjectRetile.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        String updateMsg = getUpdateMessage();
                        if(updateMsg != null) {
                            ProjectRetile.getInstance().getLog().info(updateMsg);
                        }
                    }
                });
            }
        }, 1, TimeUnit.HOURS);
    }

    public static String getUpdateMessage() {
        Version current = new Version(ProjectRetile.getInstance().getDescription().getVersion());
        Version newest = getNewestVersion();

        if(current.compareTo(newest) < 0) {
            return "There is a new Version available: " + newest.toString() + " (You are running " + current.toString() + ")";
        } else if(current.compareTo(newest) != 0) {
            if((current.getTag().toUpperCase().startsWith("DEV")) || (current.getTag().toUpperCase().startsWith("SNAPSHOT")) ||
               (current.getTag().toUpperCase().startsWith("PRE")) || (current.getTag().toUpperCase().startsWith("PRERELEASE"))) {
                return "You are running a developement Version of ProjectRetile! Please report any Bugs to our GitHub Page.";
            } else if(current.getTag().toUpperCase().equals("OFFLINE")) {
                return "Could not check for Updates. Please check your Internet Connection.";
            } else {
                return "You are running a newer Version than released!";
            }
        }

        return null;
    }

    public static Version getNewestVersion() throws IllegalArgumentException {
        if(ProjectRetile.getInstance().getConfig().forceAsyncRequests) {
            try {
                Future<Response> f = ProjectRetile.getInstance().getClient().prepareGet(URL + PLUGIN + SUBURL + "?" + System.currentTimeMillis())
                        .addQueryParam("User-Agent", "ProjectRetile v" + ProjectRetile.getInstance().getDescription().getVersion())
                        .execute();
                Response r = f.get();

                JsonObject obj = Json.parse(new InputStreamReader(r.getResponseBodyAsStream())).asObject();
                return new Version(obj.get("name").asString());
            } catch (InterruptedException | ExecutionException | IOException ex) {
                ProjectRetile.getInstance().getLog().error("Could not resolve latest Version. Please check your Internet Connection.", ex);
                return new Version(ProjectRetile.getInstance().getDescription().getVersion() + "-OFFLINE");
            }
        } else {
            try {
                URL url = new URL(URL + PLUGIN + SUBURL + "?" + System.currentTimeMillis());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("User-Agent", "ProjectRetile v" + ProjectRetile.getInstance().getDescription().getVersion());
                connection.setUseCaches(true);
                connection.setDoOutput(true);

                JsonObject obj = Json.parse(new InputStreamReader(connection.getInputStream())).asObject();
                return new Version(obj.get("name").asString());
            } catch (IOException ex) {
                ProjectRetile.getInstance().getLog().error("Could not resolve latest Version. Please check your Internet Connection.", ex);
                return new Version(ProjectRetile.getInstance().getDescription().getVersion() + "-OFFLINE");
            }
        }
    }

}
