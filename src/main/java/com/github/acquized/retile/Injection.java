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
package com.github.acquized.retile;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import com.github.acquized.retile.api.RetileAPI;
import com.github.acquized.retile.api.RetileAPIProvider;
import com.github.acquized.retile.cache.Cache;
import com.github.acquized.retile.cache.impl.McAPICanada;
import com.github.acquized.retile.cache.impl.Offline;
import com.github.acquized.retile.i18n.I18n;

import net.md_5.bungee.api.ProxyServer;

public class Injection extends AbstractModule {

    @Override
    protected void configure() {
        bind(ProjectRetile.class);
        bind(RetileAPI.class).to(RetileAPIProvider.class);
        bind(I18n.class);
    }

    public static class CacheInjection extends AbstractModule {

        private ProjectRetile retile;

        @Inject
        public CacheInjection(ProjectRetile retile) {
            this.retile = retile;
        }

        @Override
        protected void configure() {
            if((ProxyServer.getInstance().getConfig().isOnlineMode()) && (!retile.getConfig().forceOfflineUUID)) {
                bind(Cache.class).to(McAPICanada.class);
            } else {
                bind(Cache.class).to(Offline.class);
            }
        }

    }

}
