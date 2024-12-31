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

package allay.master.service;

import allay.api.network.packet.packets.service.ServicePacket;
import allay.api.service.CloudService;
import allay.master.AllayMaster;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public class ServiceQueue {

    private final AllayMaster allayMaster;
    private final ServiceManager serviceManager;

    private final Queue<CloudService> queue = new ConcurrentLinkedQueue<>();

    public void init() {
        Thread queueThread = new Thread(() -> {
            while (!(allayMaster.shuttingDown())) {
                allayMaster.sleep(1000);
                process();
            }
        });
        queueThread.setName("ServiceQueue");
        queueThread.start();
    }

    private void process() {
        Queue<CloudService> tempQueue = new ConcurrentLinkedQueue<>(queue);
        tempQueue.removeIf(service -> (service.node() == null || allayMaster.networkManager().channel(service.node()) == null));

        if (tempQueue.isEmpty()) return;

        CloudService service = tempQueue.peek();
        ServicePacket feedback = (ServicePacket) allayMaster.networkManager().channel(service.node()).sendAndReceive(new ServicePacket(service, ServicePacket.Action.START)).join();
        service.ip(feedback.service().ip());
        service.port(feedback.service().port());
        service.state(feedback.service().state());

        serviceManager.services().get(service.group()).add(service);
        queue.remove(service);
    }

    public void add(CloudService service) {
        queue.add(service);
    }

}
