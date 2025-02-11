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

import allay.api.network.packet.packets.service.ServiceAuthPacket;
import allay.api.network.packet.packets.service.ServiceDisablePacket;
import allay.api.network.packet.packets.service.ServicePacket;
import allay.api.network.util.NetworkUtil;
import allay.api.service.CloudService;
import allay.api.service.CloudServiceState;
import allay.api.util.FileUtil;
import allay.node.AllayNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public class ServiceManager {

    public static String VELOCITY_SECRET = null;

    private final AllayNode allayNode;
    private final HashMap<UUID, RunningService> services = new HashMap<>();

    public void load() {
        allayNode.networkManager().addListener(ServicePacket.class, packet -> {
            CloudService service = packet.service();
            ServicePacket.Action action = packet.action();

            switch (action) {
                case START -> {
                    if (service.ip().equalsIgnoreCase("0.0.0.0")) {
                        service.ip(NetworkUtil.getCurrentIp());
                    }
                    if (service.port() == 0) {
                        service.port(ServiceUtil.getPort(this, service));
                    }
                    service.state(CloudServiceState.STARTING);

                    RunningService runningService = new RunningService(allayNode, service);
                    runningService.start();

                    services.put(runningService.service().systemId(), runningService);
                    allayNode.sleep(50); // todo - uhm we had some weird netty errors here - this is a quick fix
                    allayNode.networkManager().channel().send(new ServicePacket(service, action).packetKey(packet.packetKey())); // creating a new packet because the service object is modified
                }

                case STOP -> {
                    service(service.systemId()).shutdown(false);
                    allayNode.networkManager().channel().send(packet);
                }

                case KILL -> {
                    service(service.systemId()).shutdown(true);
                    allayNode.networkManager().channel().send(packet);
                }
            }
        });

        allayNode.networkManager().addListener(ServiceAuthPacket.class, packet -> {
            RunningService runningService = service(packet.systemId());

            if (runningService != null) {
                runningService.service().state(CloudServiceState.ONLINE);
            }
        });

        allayNode.networkManager().addListener(ServiceDisablePacket.class, packet -> {
            RunningService runningService = service(packet.systemId());
            if (runningService == null) return;

            runningService.shutdown(true);
        });
    }

    public void shutdown() {
        // we sort the services by name - just for the esthetics
        services.values().stream().sorted(Comparator.comparing(service -> service.service().displayName())).forEach(service -> service.shutdown(true));
        FileUtil.delete(allayNode.logger(), new File("dynamic/"));
    }

    public RunningService service(UUID systemId) {
        return services.getOrDefault(systemId, null);
    }

    public RunningService service(String input) {
        return services.values().stream().filter(runningService -> runningService.service().systemId().toString().equals(input) || runningService.service().displayName().equalsIgnoreCase(input) || (runningService.service().group().name() + runningService.service().orderId()).equalsIgnoreCase(input)).findFirst().orElse(null);
    }

}
