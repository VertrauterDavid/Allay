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

package allay.master.web;

import allay.master.AllayMaster;
import allay.master.web.test.*;
import lombok.RequiredArgsConstructor;
import spark.Spark;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class WebManager {

    private final AllayMaster allayMaster;
    private final int port;

    public CompletableFuture<Void> boot() {
        return CompletableFuture.runAsync(() -> {

            Spark.staticFiles.location("/public");
            Spark.port(port);

            Spark.before(((request, response) -> {
                String key = request.headers("key");
                if (key == null || !(key.equalsIgnoreCase(allayMaster.networkManager().authToken()))) {
                    response.status(401);
                    response.body("Unauthorized");
                    Spark.halt(401, "Unauthorized");
                }
            }));

            new PingRoute(allayMaster);
            new ProxiesRoute(allayMaster);
            new ServersRoute(allayMaster);
            new ServiceNodeHostnameRoute(allayMaster);
            new ServicesRoute(allayMaster);

            Spark.init();
            Spark.awaitInitialization();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                Spark.stop();
                Spark.awaitStop();
            }));

        });
    }

}
