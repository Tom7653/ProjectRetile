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
package com.github.acquized.retile.notifications;

import com.github.acquized.retile.ProjectRetile;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

public class Notifications {

    @Getter @Setter private static Notifications instance;

    private List<UUID> receiving = new ArrayList<>();

    public boolean isReceiving(ProxiedPlayer p) {
        return receiving.contains(ProjectRetile.getInstance().getCache().uuid(p.getName()));
    }

    public void setReceiving(ProxiedPlayer p) {
        receiving.add(ProjectRetile.getInstance().getCache().uuid(p.getName()));
    }

    public void unsetReceiving(ProxiedPlayer p) {
        receiving.remove(ProjectRetile.getInstance().getCache().uuid(p.getName()));
    }

}
