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

import allay.api.service.CloudGroup;
import allay.api.service.CloudService;
import allay.api.service.CloudServiceState;
import allay.master.AllayMaster;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ServiceStarter {

    // todo - check on process if group still exists
    // todo - thread to remove services from queue if the group no longer exists

    private final AllayMaster allayMaster;
    private final ServiceManager serviceManager;

    public void init() {
        Thread queueThread = new Thread(() -> {
            while (!(allayMaster.shuttingDown())) {
                allayMaster.sleep(1000);
                process();
            }
        });
        queueThread.setName("ServiceStarter");
        queueThread.start();
    }

    private void process() {
        serviceManager.services().keySet().forEach(this::process);
    }

    private void process(CloudGroup group) {
        group.nodes().forEach((node, amount) -> {
            long running = serviceManager.services().get(group).stream().filter(service -> service.node().equals(node)).count();
            long queued = serviceManager.queue().queue().stream().filter(service -> service.group().equals(group) && service.node().equals(node)).count();
            long startAmount = amount - (running + queued);

            if (startAmount <= 0) return;
            process(group, node, startAmount);
        });
    }

    private void process(CloudGroup group, String node, long amount) {
        for (long l = 0; l < amount; l++) {
            // displayName and order id will be set by the queue
            // ip and port will be set by the node but must be defined for the packet
            CloudService service = new CloudService("-", group, CloudServiceState.QUEUE, UUID.randomUUID(), 0, node, "0.0.0.0", 0);
            serviceManager.queue().add(service);
        }
    }

}
