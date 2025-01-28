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

import allay.api.network.packet.packets.service.ServicePacket;
import allay.api.service.CloudService;
import allay.api.service.CloudServiceState;
import allay.api.service.ServiceVersion;
import allay.api.util.FileUtil;
import allay.node.AllayNode;
import allay.node.service.config.BukkitConfig;
import allay.node.service.config.VeloConfig;
import allay.node.service.thread.OutputThread;
import allay.node.service.thread.RegisterThread;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
@Setter
public class RunningService {

    private final AllayNode allayNode;
    private final CloudService service;

    private File directory;
    private Process process = null;
    private OutputStream outputStream = null;
    private AtomicReference<String> output = null;

    public void start() {
        allayNode.logger().info("Starting service §a" + service.displayName() + "§r on §a" + service.hostname() + "§r...");

        directory = new File((service.group().staticGroup() ? "static/" : "dynamic/") + service.displayName());

        if (directory.exists()) {
            FileUtil.delete(allayNode.logger(), directory);
            allayNode.logger().warning("Deleted existing service directory for service §c" + service.displayName() + "§r!");
        }

        if (!(directory.mkdirs())) {
            allayNode.logger().error("Failed to create service directory for service §c" + service.displayName() + "§r!");
            return;
        }

        try {
            FileUtil.copyFile(service.group().version().jarFile(), new File(directory, service.group().version().jarFile().getName()));
            FileUtil.copyFile(new File("storage/files/", "server-icon.png"), new File(directory, "server-icon.png"));
            FileUtil.copyFile(new File("storage/files/", "AllayPlugin.jar"), new File(directory.getPath() + "/plugins/", "AllayPlugin.jar"));

            FileUtil.copyFiles(new File("templates/" + (service.group().version() == ServiceVersion.VELOCITY_LATEST ? "GLOBAL_PROXY" : "GLOBAL_SERVER") + "/"), directory.getPath());
            FileUtil.copyFiles(new File("templates/" + service.group().name().toUpperCase() + "/"), directory.getPath());
            service.group().templates().forEach(template -> FileUtil.copyFiles(new File("templates/" + template + "/"), directory.getPath()));

            if (service.group().version() == ServiceVersion.VELOCITY_LATEST) {
                VeloConfig.setup1(this);
                VeloConfig.setup2(this);
            } else {
                BukkitConfig.setup1(this);
                BukkitConfig.setup2(this);
                BukkitConfig.setupPaperVelo(this);
            }

            ProcessBuilder processBuilder = getProcessBuilder();
            // processBuilder.inheritIO();

            processBuilder.environment().put("ALLAY_SERVICE_NAME", service.displayName());
            processBuilder.environment().put("ALLAY_NETWORK_SYSTEM_ID", allayNode.networkManager().id());
            processBuilder.environment().put("ALLAY_NETWORK_AUTH_TOKEN", allayNode.networkManager().authToken());
            processBuilder.environment().put("ALLAY_NETWORK_HOST", allayNode.networkManager().host());
            processBuilder.environment().put("ALLAY_NETWORK_PORT", String.valueOf(allayNode.networkManager().port()));
            service.group().environment().forEach(processBuilder.environment()::put);
            if (service.group().version() == ServiceVersion.VELOCITY_LATEST) {
                processBuilder.environment().put("REDISBUNGEE_PROXY_ID", service.displayName());
            }

            process = processBuilder.start();
            outputStream = process.getOutputStream();

            OutputThread outputThread = new OutputThread(this);
            outputThread.setName("ServiceOutputThread-" + service.systemId());
            outputThread.start();

            RegisterThread registerThread = new RegisterThread(this);
            registerThread.setName("ServiceRegisterThread-" + service.systemId());
            registerThread.start();

        } catch (IOException exception) {
            allayNode.logger().error("Failed to start service §c" + service.displayName() + "§r!");
            allayNode.logger().exception(exception);
        }
    }

    private @NotNull ProcessBuilder getProcessBuilder() {
        List<String> commands = Arrays.asList(service.group().startupCommand().split(" "));
        commands.replaceAll(command -> command
                .replaceAll("%java%", service.group().javaVersion().command())
                .replaceAll("%jarFile%", service.group().version().jarFile().getName())
                .replaceAll("%memory%", String.valueOf(service.group().memory()))
                .replaceAll("%port%", String.valueOf(service.port()))
        );

        // idk why, but we had it in midgard...
        allayNode.sleep(500);

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(directory);
        return processBuilder;
    }

    public void shutdown(boolean force) {
        if (!(force) && service.state() != CloudServiceState.STOPPING) {
            service.state(CloudServiceState.STOPPING);
            allayNode.logger().info("Stopping service §c" + service.displayName() + "§r...");
            execute(service.group().version().shutdownCommand());
            return;
        }

        service.state(CloudServiceState.STOPPING);
        allayNode.logger().info("Killing service §c" + service.displayName() + "§r...");

        if (process != null) {
            process.destroy();
            process = null;
            outputStream = null;
        }

        if (!(service.group().staticGroup())) {
            FileUtil.delete(allayNode.logger(), directory);
        }

        allayNode.serviceManager().services().remove(service.systemId());
        allayNode.networkManager().channel().sendIfActive(new ServicePacket(service, ServicePacket.Action.UNREGISTER));
        allayNode.sleep(200);
    }

    public void execute(String command) {
        if (process == null) return;
        if (outputStream == null) return;

        try {
            outputStream.write((command + "\n").getBytes());
            outputStream.flush();
        } catch (Exception ignored) { }
    }

    public void check() {
        if (!(allayNode.serviceManager().services().containsKey(service.systemId()))) return;

        if (process == null || !(process.isAlive())) {
            allayNode.serviceManager().services().remove(service.systemId());
            allayNode.networkManager().channel().sendIfActive(new ServicePacket(service, ServicePacket.Action.UNREGISTER));
        }
    }

}
