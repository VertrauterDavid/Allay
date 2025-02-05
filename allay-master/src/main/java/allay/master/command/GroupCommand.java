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
import allay.api.service.JavaVersion;
import allay.api.service.ServiceVersion;
import allay.api.util.JsonFile;
import allay.master.AllayMaster;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unused")
public class GroupCommand extends Command<AllayMaster> {

    public GroupCommand(AllayMaster allayMaster) {
        super(allayMaster, "group");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 6) {
            if (args[0].equalsIgnoreCase("create")) {
                try {
                    String name = args[1];
                    ServiceVersion software = ServiceVersion.fromString(args[2]);
                    int memory = Integer.parseInt(args[3]);
                    int minOnline = Integer.parseInt(args[4]);
                    boolean staticGroup = Boolean.parseBoolean(args[5]);

                    if (name.length() < 3 || name.length() > 16) {
                        allayInstance.logger().error("§cThe name must be between 3 and 16 characters.");
                        return;
                    }

                    if (!(name.matches("[a-zA-Z0-9]*"))) {
                        allayInstance.logger().error("§cThe name must be alphanumeric.");
                        return;
                    }

                    if (allayInstance.serviceManager().group(name) != null) {
                        allayInstance.logger().error("§cThe group already exists.");
                        return;
                    }

                    if (software == null) {
                        allayInstance.logger().error("§cThe software is not supported.");
                        return;
                    }

                    if (!(memory % 256 == 0)) {
                        allayInstance.logger().error("§cThe memory must be a multiple of 256.");
                        return;
                    }

                    if (memory < 512 || memory > 65536) {
                        allayInstance.logger().error("§cThe memory must be between 512 and 65536mb.");
                        return;
                    }

                    if (minOnline < 1 || minOnline > 100) {
                        allayInstance.logger().error("§cThe minimum online must be between 1 and 100.");
                        return;
                    }

                    CloudGroup group = new CloudGroup(name, "default", memory, minOnline, -1, staticGroup, software, JavaVersion.DEFAULT, new HashMap<>(), new ArrayList<>(), "default.json", new JsonFile(new File("storage/groups/startup/default.json")).getString("command"), new HashMap<>());
                    allayInstance.serviceManager().services().put(group, new ArrayList<>());
                    allayInstance.serviceManager().save(group);
                    allayInstance.logger().info("§aThe group has been created!");
                } catch (NumberFormatException ignored) { }
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete")) {
                String name = args[1];
                CloudGroup group = allayInstance.serviceManager().group(name);

                if (group == null) {
                    allayInstance.logger().error("§cThe group does not exist.");
                    return;
                }

                // todo - stop running services and unregister from the network + from the nodes

                allayInstance.serviceManager().services().remove(group);
                allayInstance.serviceManager().delete(group);
                allayInstance.logger().info("§aThe group has been deleted!");
            }
        }

        allayInstance.logger().info("│");
        allayInstance.logger().info("│ Usage: §9group create [name] [software] [memory] [minOnline] [static]");
        allayInstance.logger().info("│ Usage: §9group delete [name]");
        allayInstance.logger().info("│");
    }

}
