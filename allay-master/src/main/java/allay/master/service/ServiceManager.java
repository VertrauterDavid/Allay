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

import allay.api.network.packet.packets.service.ServiceCommandPacket;
import allay.api.network.packet.packets.service.ServiceDisablePacket;
import allay.api.network.packet.packets.service.ServicePacket;
import allay.api.service.*;
import allay.api.util.FileUtil;
import allay.api.util.JsonFile;
import allay.api.util.StringUtil;
import allay.master.AllayMaster;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Accessors(fluent = true)
@Getter
public class ServiceManager {

    public static String VELOCITY_SECRET = StringUtil.generateRandom(64);

    private final AllayMaster allayMaster;
    private final ServiceQueue queue;
    private final ServiceStarter starter;
    private final HashMap<CloudGroup, ArrayList<CloudService>> services = new HashMap<>();

    public ServiceManager(AllayMaster allayMaster) {
        this.allayMaster = allayMaster;

        this.queue = new ServiceQueue(allayMaster, this);
        this.queue.init();

        this.starter = new ServiceStarter(allayMaster, this);
        this.starter.init();
    }

    public void load() {
        loadStartup();
        loadGroups();
        addListener();
    }

    private void loadStartup() {
        File groups = new File("storage/groups/startup");
        if (!(groups.exists()) && !(groups.mkdirs())) return;

        ServiceUtil.defaultStartup();
    }

    private void loadGroups() {
        File groups = new File("storage/groups");
        if (!(groups.exists()) && !(groups.mkdirs())) return;

        Arrays.stream(Objects.requireNonNull(groups.listFiles())).forEach(file -> {
            if (file.getName().endsWith(".json")) {
                load(file.getName().replace(".json", ""));
            }
        });
    }

    private void load(String name) {
        if (name == null || name.contains(" ")) throw new IllegalArgumentException("Invalid group name");
        if (!(new File("storage/groups/" + name + ".json").exists())) throw new IllegalArgumentException("Group not found");

        JsonFile file = new JsonFile(new File("storage/groups/" + name + ".json"));
        if (!(name.equals(file.getString("name")))) throw new IllegalArgumentException("Group name in file does not match with the file name");

        String startupFileName = file.getString("startupFile");
        File startupFile = new File("storage/groups/startup/" + startupFileName);

        String startupCommand;
        if (startupFile.exists()) {
            startupCommand = new JsonFile(startupFile).getString("command");
        } else {
            startupCommand = new JsonFile(new File("storage/groups/startup/default.json")).getString("command");
        }

        CloudGroup group = new CloudGroup(
                file.getString("name"),
                file.getString("displayName"),
                file.getLong("memory"),
                file.getLong("minInstances"),
                file.getLong("maxInstances"),
                file.getBoolean("staticGroup"),
                ServiceVersion.valueOf(file.getString("version")),
                JavaVersion.valueOf(file.getString("javaVersion")),
                file.getMap("nodes", String.class, Long.class),
                file.getList("templates", String.class),
                startupFileName,
                startupCommand,
                file.getMap("environment", String.class, String.class)
        );
        group.environment().forEach((key, value) -> System.out.println("Key: " + key + " Value: " + value));

        services.put(group, new ArrayList<>());
    }

    private void addListener() {
        allayMaster.networkManager().addListener(ServicePacket.class, packet -> {
            CloudService service = packet.service();
            ServicePacket.Action action = packet.action();

            switch (action) {
                case UPDATE -> {
                    CloudService localServer = service(service.systemId());
                    if (localServer == null) return;

                    localServer.state(service.state());
                    localServer.systemId(service.systemId());
                    localServer.orderId(service.orderId());
                    localServer.node(service.node());
                    localServer.ip(service.ip());
                    localServer.port(service.port());
                }

                case REGISTER -> {
                    /*
                    if (!(service.group().version().proxy())) {
                        services.values().stream().flatMap(Collection::stream).forEach(proxy -> {
                            if (!(proxy.group().version().proxy())) return;

                            NetworkChannel channel = allayMaster.networkManager().channel("service-" + proxy.systemId());
                            if (channel != null) {
                                channel.send(packet);
                            }
                        });
                    }
                     */

                    // todo remove?
                    // allayMaster.logger().info("Registered service §a" + service.displayName() + "§r on §a" + service.hostname() + "§r (" + service.node() + ")");
                }

                case UNREGISTER -> {
                    CloudService localServer = service(service.systemId());
                    localServer.state(CloudServiceState.STOPPING);
                    services.get(service.group()).remove(service);

                    // send packet to all proxies
                    /*
                    services.values().stream().flatMap(Collection::stream).forEach(proxy -> {
                        if (!(proxy.group().version().proxy())) return;
                        allayMaster.networkManager().channel("service-" + proxy.systemId()).send(packet);
                    });
                     */

                    // todo remove?
                    // allayMaster.logger().info("Unregistered service §c" + service.displayName());
                }
            }
        });

        allayMaster.networkManager().addListener(ServiceDisablePacket.class, packet -> {
            CloudService service = service(packet.systemId());
            if (service == null) return;

            service.state(CloudServiceState.STOPPING);
            services.get(service.group()).remove(service);

            allayMaster.networkManager().channels().forEach(channel -> channel.send(packet));
            allayMaster.logger().info("Unregistered service §c" + service.displayName());
        });
    }

    public void save() {
        services.keySet().forEach(this::save);
    }

    public void save(CloudGroup group) {
        JsonFile file = new JsonFile(new File("storage/groups/" + group.name() + ".json"));

        file.setString("name", group.name());
        file.setString("displayName", group.displayName());
        file.setLong("memory", group.memory());
        file.setLong("minInstances", group.minInstances());
        file.setLong("maxInstances", group.maxInstances());
        file.setBoolean("staticGroup", group.staticGroup());
        file.setString("version", group.version().name());
        file.setString("javaVersion", group.javaVersion().name());
        file.setMap("nodes", group.nodes());
        file.setList("templates", group.templates());
        file.setString("startupFile", group.startupFile());
        file.setMap("environment", group.environment());
    }

    public void delete(CloudGroup group) {
        FileUtil.delete(allayMaster.logger(), new File("storage/groups/" + group.name() + ".json"));
    }

    /*
    public CompletableFuture<Void> stopService(CloudService service, boolean force) {
        return CompletableFuture.runAsync(() -> {
            allayMaster.networkManager().channel(service.node()).sendAndReceive(new ServicePacket(service, (force ? ServicePacket.Action.KILL : ServicePacket.Action.STOP))).join();
        });
    }
     */

    public CompletableFuture<Void> execute(CloudGroup group, String command) {
        return CompletableFuture.runAsync(() -> services.get(group).forEach(service -> execute(service, command).join()));
    }

    public CompletableFuture<Void> execute(CloudService service, String command) {
        return CompletableFuture.runAsync(() -> allayMaster.networkManager().channel("service-" + service.node()).send(new ServiceCommandPacket(command)));
    }

    public CloudGroup group(String input) {
        return services.keySet().stream().filter(group -> group.name().equalsIgnoreCase(input) || group.displayName().equalsIgnoreCase(input)).findFirst().orElse(null);
    }

    public CloudService service(UUID systemId) {
        return services.values().stream().flatMap(Collection::stream).filter(service -> service.systemId().equals(systemId)).findFirst().orElse(null);
    }

    public CloudService service(String input) {
        return services.values().stream().flatMap(Collection::stream).filter(service -> service.systemId().toString().equals(input) || service.displayName().equalsIgnoreCase(input) || (service.group().name() + service.orderId()).equalsIgnoreCase(input)).findFirst().orElse(null);
    }

}
