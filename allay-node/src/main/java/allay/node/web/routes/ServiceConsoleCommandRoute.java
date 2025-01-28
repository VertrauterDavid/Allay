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

package allay.node.web.routes;

import allay.node.AllayNode;
import allay.node.service.RunningService;
import allay.node.web.method.PostRoute;
import spark.Request;
import spark.Response;

public class ServiceConsoleCommandRoute extends PostRoute {

    public ServiceConsoleCommandRoute(AllayNode allayNode) {
        super(allayNode, "/service/console/command");
    }

    @Override
    public void onRequest(Request request, Response response) {
        String service = request.queryParams("service");
        String command = request.queryParams("command");
        RunningService runningService = allayNode.serviceManager().service(service);

        if (runningService == null) {
            response.status(404);
            response.body("Service not found :(");
            return;
        }

        runningService.execute(command);

        response.status(200);
        response.body("command executed");
    }

}
