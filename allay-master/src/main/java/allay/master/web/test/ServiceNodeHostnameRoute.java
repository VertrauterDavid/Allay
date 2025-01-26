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

package allay.master.web.test;

import allay.api.service.CloudService;
import allay.master.AllayMaster;
import allay.master.web.method.GetRoute;
import spark.Request;
import spark.Response;

public class ServiceNodeHostnameRoute extends GetRoute {

    public ServiceNodeHostnameRoute(AllayMaster allayMaster) {
        super(allayMaster, "/service/node/hostname");
    }

    @Override
    public void onRequest(Request request, Response response) {
        String service = request.queryParams("service");
        CloudService cloudService = allayMaster.serviceManager().service(service);

        if (cloudService == null) {
            response.status(404);
            response.body("Service not found :(");
            return;
        }

        response.status(200);
        response.body(allayMaster.networkManager().channel(cloudService.node()).hostname() + ":8050"); // todo - get correct port
    }

}
