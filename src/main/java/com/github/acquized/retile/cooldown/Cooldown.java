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

import com.github.acquized.retile.ProjectRetile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;

public class Cooldown {

    private Map<UUID, Long> watches = new HashMap<>();
    @Getter @Setter private static Cooldown instance;

    public void start(UUID uuid) {
        watches.put(uuid, System.currentTimeMillis());
    }

    public boolean inCooldown(UUID uuid) {
        if((watches.containsKey(uuid)) && (TimeUnit.MILLISECONDS.toSeconds(watches.get(uuid)) < ProjectRetile.getInstance().getConfig().cooldown)) {
            return true;
        } else if(TimeUnit.MILLISECONDS.toSeconds(watches.get(uuid)) >= ProjectRetile.getInstance().getConfig().cooldown) {
            stop(uuid);
            return false;
        }
        return false;
    }

    public long getRemaining(UUID uuid, TimeUnit timeUnit) {
        if(watches.get(uuid) != null) {
            return timeUnit.convert(watches.get(uuid), TimeUnit.MILLISECONDS);
        }
        return -1;
    }

    public void stop(UUID uuid) {
        watches.remove(uuid);
    }

}
