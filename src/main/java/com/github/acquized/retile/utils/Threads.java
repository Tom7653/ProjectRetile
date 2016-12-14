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
package com.github.acquized.retile.utils;

import com.github.acquized.retile.ProjectRetile;

import net.md_5.bungee.api.ProxyServer;

import java.util.concurrent.TimeUnit;

public class Threads {

    public static void sync(Runnable r) {
        ProxyServer.getInstance().getScheduler().schedule(ProjectRetile.getInstance(), r, 0, TimeUnit.MILLISECONDS);
    }

    public static void async(Runnable r) {
        ProxyServer.getInstance().getScheduler().runAsync(ProjectRetile.getInstance(), r);
    }

    /*
     * TODO
     * This is currently only a wrapper so I don't need to type the long sync and async commands.
     * As soon as available (for Bungee), we need to switch to https://github.com/aikar/TaskChain
     *
     */

}
