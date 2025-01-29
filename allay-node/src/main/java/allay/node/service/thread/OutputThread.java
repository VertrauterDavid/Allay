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
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class OutputThread extends Thread {

    private final RunningService runningService;

    @Override
    public void run() {
        runningService.output(new AtomicReference<>());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(runningService.process().getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                /*
                // todo needed for proxy log?
                if (line.contains("connected player")) {
                    continue;
                }
                 */

                // replace < and > with &lt; and &gt; to prevent html injection
                line = line.replace("<", "&lt;").replace(">", "&gt;");

                runningService.output().accumulateAndGet(line + "\n", (prev, curr) -> prev + curr);
            }
        } catch (IOException ignored) { }
    }

}
