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
package com.github.acquized.retile.hub;

import com.google.common.base.Stopwatch;

import com.github.acquized.retile.ProjectRetile;

import net.md_5.bungee.api.ProxyServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;

public class Cooldown {

    @Getter @Setter private static Cooldown instance;
    public Map<UUID, Stopwatch> cooldown = new HashMap<>();

    public Cooldown() {
        ProxyServer.getInstance().getScheduler().schedule(ProjectRetile.getInstance(), new Runnable() {
            @Override
            public void run() {
                for(Map.Entry<UUID, Stopwatch> entry : cooldown.entrySet()) {
                    if(entry.getValue().elapsed(TimeUnit.SECONDS) == ProjectRetile.getInstance().getConfig().cooldown) {
                        cooldown.remove(entry.getKey());
                    }
                }
            }
        }, 1, TimeUnit.SECONDS);
    }

    public boolean in(UUID uuid) {
        return cooldown.containsKey(uuid);
    }

    public void start(UUID uuid) {
        cooldown.put(uuid, Stopwatch.createStarted());
    }

    public void stop(UUID uuid) {
        cooldown.remove(uuid);
    }

}
