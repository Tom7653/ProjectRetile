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
package com.github.acquized.retile.ui;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import dev.wolveringer.BungeeUtil.BungeeUtil;
import dev.wolveringer.BungeeUtil.Player;
import lombok.Getter;

public class Bridge {

    @Getter private static Bridge bridge;
    private BungeeUtil instance;

    public Bridge(BungeeUtil instance) {
        this.instance = instance;
        inject();
    }

    public InventoryBuilder inventory(int slots, String title) {
        return new InventoryBuilder(title, slots);
    }

    public Player getPlayer(ProxiedPlayer player) {
        return (Player) player;
    }

    public void inject() {
        if(!instance.isInjected()) {
            instance.inject();
        }
    }

    // -----------------------------------------

    public static void injectBridge() {
        bridge = new Bridge(BungeeUtil.getInstance());
    }

}
