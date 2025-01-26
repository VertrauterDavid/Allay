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

import allay.master.AllayMaster;
import allay.master.web.method.GetRoute;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

public class ServersRoute extends GetRoute {

    public ServersRoute(AllayMaster allayMaster) {
        super(allayMaster, "/servers");
    }

    @Override
    public void onRequest(Request request, Response response) {
        ArrayList<Service> services = new ArrayList<>();
        allayMaster.serviceManager().services().forEach((cloudGroup, list) -> {
            if (cloudGroup.version().proxy() || cloudGroup.version().bedrock()) return;
            list.forEach(cloudService -> {
                String node = cloudService.node();
                String nodeHostname = allayMaster.networkManager().channel(node).hostname();

                services.add(new Service(cloudService.displayName(), cloudService.systemId(), node, nodeHostname, cloudService.hostname()));
            });
        });
        services.sort(Comparator.comparing(Service::displayName));

        response.status(200);
        response.body(new Gson().toJson(services));
    }

    private record Service(String displayName, UUID systemId, String node, String nodeHostname, String serverHostname) {}

}
