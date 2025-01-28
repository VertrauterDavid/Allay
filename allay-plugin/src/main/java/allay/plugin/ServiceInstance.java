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

package allay.plugin;

import allay.api.AllayApi;
import allay.api.network.packet.packets.service.ServiceCommandPacket;
import allay.plugin.network.NetworkConfig;
import allay.plugin.network.NetworkManager;

import java.util.UUID;

public abstract class ServiceInstance {

    private NetworkManager networkManager;

    public void enable() {
        AllayApi.instance(new AllayApi());

        NetworkConfig networkConfig = new NetworkConfig(
                UUID.fromString(System.getenv("ALLAY_SERVICE_ID")),
                System.getenv("ALLAY_NETWORK_AUTH_TOKEN"),
                System.getenv("ALLAY_NETWORK_HOST"),
                Integer.parseInt(System.getenv("ALLAY_NETWORK_PORT"))
        );
        networkManager = new NetworkManager(networkConfig);
        networkManager.bootSync();

        networkManager.addListener(ServiceCommandPacket.class, packet -> execute(packet.command()));
    }

    public void disable() {
        networkManager.shutdownSync();
    }

    public abstract void execute(String command);

}
