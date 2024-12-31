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

import allay.api.service.CloudService;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

@UtilityClass
public class ServiceUtil {

    public static int getPort(ServiceManager serviceManager, CloudService service) {
        int port = service.group().version().startPort();

        while (isPortUsed(serviceManager, port)) {
            port++;

            if (port > 60000) {
                throw new IllegalStateException("No free port found");
            }
        }

        return port;
    }

    private static boolean isPortUsed(ServiceManager serviceManager, int port) {
        for (RunningService service : serviceManager.services().values()) {
            if (service.service().port() == port) return true;
        }

        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(port));
            return false;
        } catch (IOException exception) {
            return true;
        }
    }

}
