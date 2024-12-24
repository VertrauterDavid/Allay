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

package allay.master.service;

import allay.api.util.JsonFile;
import allay.master.AllayMaster;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@RequiredArgsConstructor
public class ServiceManager {

    private final AllayMaster allayMaster;
    private final ArrayList<CloudGroupImpl> groups = new ArrayList<>();

    public void load() {
        File groups = new File("groups");
        if (!(groups.exists())) groups.mkdirs();

        Arrays.stream(Objects.requireNonNull(groups.listFiles())).forEach(file -> {
            if (file.getName().endsWith(".json")) {
                load(file.getName().replace(".json", ""));
            }
        });
    }

    public void load(String name) {
        if (name == null || name.contains(" ")) throw new IllegalArgumentException("Invalid group name");
        if (!(new File("groups/" + name.toUpperCase() + ".json").exists())) throw new IllegalArgumentException("Group not found");

        JsonFile file = new JsonFile(new File("groups/" + name.toUpperCase() + ".json"));
        if (!(name.equals(file.getString("name")))) throw new IllegalArgumentException("Group name in file does not match with the file name");

        CloudGroupImpl group = new CloudGroupImpl(
                allayMaster,
                file.getString("name"),
                file.getLong("memory"),
                file.getLong("minInstances"),
                file.getLong("maxInstances"),
                file.getBoolean("staticGroup"),
                file.getString("version"),
                file.getString("javaVersion"),
                file.getMap("nodes", String.class, Long.class),
                file.getList("templates", String.class)
        );

        groups.add(group);
    }

    public void save() {
        groups.forEach(this::save);
    }

    public void save(CloudGroupImpl group) {
        JsonFile file = new JsonFile(new File("groups/" + group.name().toUpperCase() + ".json"));

        file.setString("name", group.name());
        file.setLong("memory", group.memory());
        file.setLong("minInstances", group.minInstances());
        file.setLong("maxInstances", group.maxInstances());
        file.setBoolean("staticGroup", group.staticGroup());
        file.setString("version", group.version());
        file.setString("javaVersion", group.javaVersion());
        file.setMap("nodes", group.nodes());
        file.setList("templates", group.templates());
    }

    public void sendUpdate(CloudGroupImpl group) {
        // todo: send update packet to all connected nodes and update their variables
    }

}
