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

package allay.node.service.thread;

import allay.node.service.RunningService;
import allay.node.service.ServiceManager;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class CheckThread extends Thread{

    private final ServiceManager serviceManager;

    @Override
    public void run() {
        while (!(serviceManager.allayNode().shuttingDown())) {
            try {
                Thread.sleep(1000);
                new ArrayList<>(serviceManager.services().values()).stream().filter(service -> service.output() != null).forEach(RunningService::check);
            } catch (InterruptedException ignored) { }
        }
    }

}

