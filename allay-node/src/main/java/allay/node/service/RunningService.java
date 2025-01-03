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

package allay.node.service;

import allay.api.network.packet.Packet;
import allay.api.network.packet.packets.RedirectToServicePacket;
import allay.api.network.packet.packets.service.ServicePacket;
import allay.api.service.CloudService;
import allay.api.service.CloudServiceState;
import allay.node.AllayNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
@Setter
public class RunningService {

    private final AllayNode allayNode;
    private final CloudService service;

    public void start() {
        allayNode.logger().info("Starting service §a" + service.name() + "§r on §a" + service.hostname() + "§r...");

        // normally we would have a delay here because the process is starting - we just simulate it for now
        allayNode.sleep(500);

        // send packet to register the service on all proxies
        allayNode.networkManager().channel().send(new ServicePacket(service, ServicePacket.Action.REGISTER));
    }

    public void shutdown(boolean force) {
        allayNode.logger().info("Stopping service §c" + service.name() + "§r...");

        // send packet to unregister the service on all proxies and from the masters service manager
        service.state(CloudServiceState.STOPPING);
        allayNode.networkManager().channel().sendIfActive(new ServicePacket(service, ServicePacket.Action.UNREGISTER));

        allayNode.sleep(200);
    }

    public void execute(String command) {
        // write the command to the process input stream
    }

    public void sendPacket(Packet packet) {
        allayNode.networkManager().channel().send(new RedirectToServicePacket(service.systemId(), packet));
    }

}
