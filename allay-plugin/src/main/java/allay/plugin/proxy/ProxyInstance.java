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

package allay.plugin.proxy;

import allay.api.network.packet.packets.service.ServicePacket;
import allay.api.service.CloudService;
import allay.plugin.ServiceInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class ProxyInstance extends ServiceInstance {

    protected final List<String> defaultServers = new ArrayList<>();

    @Override
    public void enable() {
        super.enable();

        networkManager.addListener(ServicePacket.class, packet -> {
            CloudService service = packet.service();
            ServicePacket.Action action = packet.action();

            switch (action) {
                case REGISTER -> {
                    registerServer(service);
                }

                case UNREGISTER -> {
                    unregisterServer(service);
                }
            }
        });
    }

    public String randomLobby() {
        return defaultServers.get(new Random().nextInt(defaultServers.size()));
    }

    public abstract void registerServer(CloudService service);
    public abstract void unregisterServer(CloudService service);

    public abstract void message(UUID uuid, String message);
    public abstract void title(UUID uuid, String title, String subTitle, int fadeIn, int stay, int fadeOut);
    public abstract void actionbar(UUID uuid, String message);
    public abstract void connect(UUID uuid, String serviceName);

}
