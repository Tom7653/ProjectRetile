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
package com.github.acquized.retile.cooldown;

import com.google.common.base.Stopwatch;

import com.github.acquized.retile.ProjectRetile;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;

public class CooldownManager {

    @Getter @Setter private static CooldownManager instance;

    private List<Cooldown> cooldowns = new ArrayList<>();

    public CooldownManager() {
        ProxyServer.getInstance().getScheduler().schedule(ProjectRetile.getInstance(), () -> cooldowns.stream().filter(c -> c.getWatch().elapsed(TimeUnit.SECONDS) == ProjectRetile.getInstance().getConfig().cooldown).forEach(c -> cooldowns.remove(c)), 1, TimeUnit.SECONDS);
    }

    public boolean isInCooldown(ProxiedPlayer p) {
        UUID uuid = ProjectRetile.getInstance().getCache().uuid(p.getName());
        for(Cooldown c : cooldowns) {
            UUID cUUID = ProjectRetile.getInstance().getCache().uuid(c.getPlayer().getName());
            if(uuid == cUUID) {
                return true;
            }
        }
        return false;
    }

    public void addCooldown(ProxiedPlayer p) {
        cooldowns.add(new Cooldown(p, Stopwatch.createStarted()));
    }

    public void removeCooldown(ProxiedPlayer p) {
        UUID uuid = ProjectRetile.getInstance().getCache().uuid(p.getName());
        for(Cooldown c : cooldowns) {
            UUID cUUID = ProjectRetile.getInstance().getCache().uuid(c.getPlayer().getName());
            if(uuid == cUUID) {
                cooldowns.remove(c);
            }
        }
    }

    public long getRemaining(ProxiedPlayer p, TimeUnit unit) {
        UUID uuid = ProjectRetile.getInstance().getCache().uuid(p.getName());
        for(Cooldown c : cooldowns) {
            UUID cUUID = ProjectRetile.getInstance().getCache().uuid(c.getPlayer().getName());
            if(uuid == cUUID) {
                return c.getWatch().elapsed(unit);
            }
        }
        return -1;
    }

}
