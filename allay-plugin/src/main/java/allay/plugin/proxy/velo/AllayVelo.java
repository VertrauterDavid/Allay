/**
 * Copyright 2024 https://github.com/VertrauterDavid/Allay
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package allay.plugin.proxy.velo;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.lifecycle.ProxyInitializeEvent;
import com.velocitypowered.api.event.lifecycle.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import javax.inject.Inject;

@Plugin(
        id = "allay",
        name = "Allay",
        version = "1"
)
public class AllayVelo {

    private final AllayVeloImpl impl;

    @Inject
    public AllayVelo(ProxyServer server) {
        impl = new AllayVeloImpl(server);
    }

    @Subscribe
    public void handle(ProxyInitializeEvent event) {
        impl.enable();
    }

    @Subscribe
    public void handle(ProxyShutdownEvent event) {
        impl.disable();
    }

}
