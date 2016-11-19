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
package com.github.acquized.retile.cache.impl;

import com.github.acquized.retile.annotations.Beta;
import com.github.acquized.retile.cache.Cache;

import net.md_5.bungee.api.ProxyServer;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Beta
public class Offline implements Cache {

    private McAPICanada backup = new McAPICanada();

    @Override
    public String username(UUID uuid) {
        if(ProxyServer.getInstance().getPlayer(uuid) != null) {
            return ProxyServer.getInstance().getPlayer(uuid).getName();
        }
        try {
            return backup.resolve(uuid).get();
        } catch (InterruptedException | ExecutionException ex) {
            return backup.username(uuid);
        }
    }

    @Override
    public UUID uuid(String name) {
        if(ProxyServer.getInstance().getPlayer(name) != null) {
            return ProxyServer.getInstance().getPlayer(name).getUniqueId();
        }
        try {
            return backup.resolve(name).get();
        } catch (InterruptedException | ExecutionException ex) {
            return backup.uuid(name);
        }
    }

    @Override
    public void addEntry(UUID uuid, String name) {}

    @Override
    public void removeEntry(UUID uuid) {}

}
