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
import allay.api.service.CloudGroup;
import allay.api.service.CloudService;
import allay.api.service.ServiceVersion;
import allay.master.AllayMaster;

@SuppressWarnings("unused")
public class ServiceCommand extends Command<AllayMaster> {

    public ServiceCommand(AllayMaster allayMaster) {
        super(allayMaster, "service");
        aliases.add("ser");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("start")) {
                try {
                    CloudGroup group = allayInstance.serviceManager().group(args[1]);
                    String node = args[2];
                    int amount = Integer.parseInt(args[3]);

                    if (group == null) {
                        allayInstance.logger().error("§cThe group does not exist.");
                        return;
                    }

                    if (allayInstance.networkManager().channel(node) == null) {
                        allayInstance.logger().error("§cThe node is not connected");
                        return;
                    }

                    if (amount < 1 || amount > 100) {
                        allayInstance.logger().error("§cThe amount must be between 1 and 100.");
                        return;
                    }

                    allayInstance.serviceManager().starter().process(group, node, amount);
                    allayInstance.logger().info("§a" + amount + " services was added to the queue");
                } catch (NumberFormatException ignored) { }
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("stop")) {
                String input = args[1];
                CloudGroup group = allayInstance.serviceManager().group(input);
                CloudService service = allayInstance.serviceManager().service(input);

                if (group != null) {
                    // todo shutdown all services
                    return;
                }

                if (service == null) {
                    allayInstance.logger().error("§cThe service does not exist.");
                }

                // todo shutdown service
                return;
            }
        }

        allayInstance.logger().info("│");
        allayInstance.logger().info("│ Usage: §9service start [group] [node] [amount]");
        allayInstance.logger().info("│ Usage: §9service stop [group / name / id]");
        allayInstance.logger().info("│");
    }

}
