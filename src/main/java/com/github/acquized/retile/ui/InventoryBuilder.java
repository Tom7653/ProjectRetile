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

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.ItemBuilder;
import dev.wolveringer.api.inventory.Inventory;

public class InventoryBuilder {

    private Inventory inventory;

    public InventoryBuilder(String title, int slots) {
        inventory = new Inventory(slots, title);
    }

    public InventoryBuilder open(Player p) {
        p.openInventory(inventory);
        return this;
    }

    public InventoryBuilder close(Player p) {
        p.closeInventory();
        return this;
    }

    public InventoryBuilder item(int slot, ItemBuilder item) {
        inventory.setItem(slot, item.build());
        return this;
    }

    public Inventory create() {
        return inventory;
    }

}
