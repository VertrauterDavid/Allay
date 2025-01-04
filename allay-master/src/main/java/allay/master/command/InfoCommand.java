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

package allay.master.command;

import allay.api.console.command.Command;
import allay.master.AllayMaster;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class InfoCommand extends Command<AllayMaster> {

    public InfoCommand(AllayMaster allayMaster) {
        super(allayMaster, "info");
    }

    @Override
    public void execute(String[] args) {
        allayInstance.logger().info("│");
        allayInstance.logger().info("│ Connected Nodes (" + allayInstance.networkManager().channels().size() + "):");
        allayInstance.networkManager().channels().forEach(channel -> allayInstance.logger().info("│ • " + channel.id() + " » " + channel.hostname() + " (" + channel.state().name() + ")"));
        allayInstance.logger().info("│");
        allayInstance.logger().info("│ Running Services (" + allayInstance.serviceManager().services().values().stream().mapToInt(ArrayList::size).sum() + "):");
        allayInstance.serviceManager().services().forEach((group, services) -> {
            if (services.isEmpty()) return;
            allayInstance.logger().info("│ • " + group.name() + " (" + services.size() + "):");

            final int[] i = {1};
            services.forEach(service -> {
                allayInstance.logger().info("│   " + (i[0] == services.size() ? "└" : "├") + " " + service.name() + " » " + service.hostname() + " (" + service.state().name() + ")");
                i[0]++;
            });
        });
        allayInstance.logger().info("│");
        allayInstance.logger().info("│ Service Queue (" + allayInstance.serviceManager().queue().queue().size() + "):");
        allayInstance.serviceManager().queue().grouped().forEach((node, groupMap) -> {
            allayInstance.logger().info("│ • " + node + " (" + groupMap.values().stream().mapToInt(List::size).sum() + "):");

            final int[] i = {1};
            groupMap.forEach((group, services) -> {
                allayInstance.logger().info("│   " + (i[0] == groupMap.size() ? "└" : "├") + " " + services.size() + "x " + group);
                i[0]++;
            });
        });
        allayInstance.logger().info("│");
    }

}
