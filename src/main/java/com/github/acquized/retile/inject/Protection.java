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
package com.github.acquized.retile.inject;

import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import dev.wolveringer.BungeeUtil.BungeeUtil;

import static com.github.acquized.retile.utils.Utility.RED;

public class Protection implements Listener {

    @EventHandler
    public void onPreLogin(PreLoginEvent e) {
        while(!BungeeUtil.getInstance().isInjected()) {
            e.setCancelReason(RED + "Please wait until the Network has fully started.");
            e.setCancelled(true);
        }
    }

}
