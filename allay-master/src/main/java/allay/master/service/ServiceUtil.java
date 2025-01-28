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

import allay.api.service.CloudGroup;
import allay.api.service.CloudService;
import allay.api.util.JsonFile;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class ServiceUtil {

    /*
    public static String getNode(MidgardMaster midgardMaster, ServiceGroupData serviceGroupData) {
        if (serviceGroupData.getAssignedNodes() == null || serviceGroupData.getAssignedNodes().isEmpty()) return null;

        return serviceGroupData.getAssignedNodes().stream()
                .filter(node -> midgardMaster.getServiceManager().getServiceGroups().values().stream()
                        .flatMap(serviceDatas -> serviceDatas.stream().filter(serviceData -> serviceData.getName().equals(serviceGroupData.getName())))
                        .noneMatch(serviceData -> serviceData.getNode().equalsIgnoreCase(node)))
                .min((node1, node2) -> {
                    AtomicInteger serviceCount1 = new AtomicInteger();
                    AtomicInteger serviceCount2 = new AtomicInteger();

                    midgardMaster.getServiceManager().getServiceGroups().values().forEach(serviceDatas -> serviceDatas.forEach(serviceData -> {
                        if (serviceData.getNode().equalsIgnoreCase(node1)) serviceCount1.getAndIncrement();
                        if (serviceData.getNode().equalsIgnoreCase(node2)) serviceCount2.getAndIncrement();
                    }));

                    return Integer.compare(serviceCount1.get(), serviceCount2.get());
                }).orElse(null);
    }

    public static boolean isAnyNodeAvailable(MidgardMaster midgardMaster, ServiceGroupData serviceGroupData) {
        return serviceGroupData.getAssignedNodes().stream()
                .anyMatch(node -> midgardMaster.getServiceManager().getServiceGroups().values().stream()
                        .flatMap(serviceDatas -> serviceDatas.stream().filter(serviceData -> serviceData.getName().equals(serviceGroupData.getName())))
                        .noneMatch(serviceData -> serviceData.getNode().equalsIgnoreCase(node)));
    }

    public static String getRandomNode(MidgardMaster midgardMaster) {
        List<String> channels = new ArrayList<>(midgardMaster.getNetworkManager().getChannels().keySet());
        return channels.isEmpty() ? null : channels.get(new Random().nextInt(channels.size()));
    }
     */

    public static int getOrderId(ServiceManager serviceManager, CloudGroup group) {
        Set<Integer> usedIds = serviceManager.services()
                .get(group)
                .stream()
                .map(CloudService::orderId)
                .collect(Collectors.toSet());

        int id = 1;
        while (usedIds.contains(id)) {
            id++;
        }

        return id;
    }

    public static void defaultStartup() {
        new JsonFile(new File("storage/groups/startup/default.json")).setStringDefault("command", String.join(" ", Arrays.asList(
                "%java%",
                "-Xmx%memory%M",
                "-jar", "%jarFile%",
                "-p%port%",
                "-h0.0.0.0"
        )));

        new JsonFile(new File("storage/groups/startup/bukkit.json")).setStringDefault("command", String.join(" ", Arrays.asList(
                "%java%",
                "-Dcom.mojang.eula.agree=true",
                "-XX:+UseG1GC",
                "-XX:+ParallelRefProcEnabled",
                "-XX:MaxGCPauseMillis=200",
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:+DisableExplicitGC",
                "-XX:+AlwaysPreTouch",
                "-XX:G1NewSizePercent=30",
                "-XX:G1MaxNewSizePercent=40",
                "-XX:G1HeapRegionSize=8M",
                "-XX:G1ReservePercent=20",
                "-XX:G1HeapWastePercent=5",
                "-XX:G1MixedGCCountTarget=4",
                "-XX:InitiatingHeapOccupancyPercent=15",
                "-XX:G1MixedGCLiveThresholdPercent=90",
                "-XX:G1RSetUpdatingPauseTimePercent=5",
                "-XX:SurvivorRatio=32",
                "-XX:+PerfDisableSharedMem",
                "-XX:MaxTenuringThreshold=1",
                "-Dusing.aikars.flags=https://mcflags.emc.gs",
                "-Daikars.new.flags=true",
                "-XX:-UseAdaptiveSizePolicy",
                "-XX:CompileThreshold=100",
                "-Dio.netty.recycler.maxCapacity=0",
                "-Dio.netty.recycler.maxCapacity.default=0",
                "-Djline.terminal=jline.UnsupportedTerminal",
                "-Dfile.encoding=UTF-8",
                "-Dclient.encoding.override=UTF-8",
                "-DIReallyKnowWhatIAmDoingISwear=true",
                "-Xmx%memory%M",
                "-jar", "%jarFile%",
                "-p%port%",
                "-h0.0.0.0"
        )));

        new JsonFile(new File("storage/groups/startup/velocity.json")).setStringDefault("command", String.join(" ", Arrays.asList(
                "%java%",
                "-XX:+UseG1GC",
                "-XX:G1HeapRegionSize=4M",
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:+ParallelRefProcEnabled",
                "-XX:+AlwaysPreTouch",
                "-XX:MaxInlineLevel=15",
                "-Xmx%memory%M",
                "-jar", "%jarFile%",
                "-p%port%"
        )));
    }

    /*
    public static void defaultStartup() {
        new JsonFile(new File("storage/groups/startup/default.json")).setListDefault("commands", Arrays.asList(
                "%java%",
                "-Xmx%memory%M",
                "-jar", "%jarFile%",
                "-p%port%",
                "-h0.0.0.0"
        ));

        new JsonFile(new File("storage/groups/startup/bukkit.json")).setListDefault("commands", Arrays.asList(
                "%java%",
                "-Dcom.mojang.eula.agree=true",
                "-XX:+UseG1GC",
                "-XX:+ParallelRefProcEnabled",
                "-XX:MaxGCPauseMillis=200",
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:+DisableExplicitGC",
                "-XX:+AlwaysPreTouch",
                "-XX:G1NewSizePercent=30",
                "-XX:G1MaxNewSizePercent=40",
                "-XX:G1HeapRegionSize=8M",
                "-XX:G1ReservePercent=20",
                "-XX:G1HeapWastePercent=5",
                "-XX:G1MixedGCCountTarget=4",
                "-XX:InitiatingHeapOccupancyPercent=15",
                "-XX:G1MixedGCLiveThresholdPercent=90",
                "-XX:G1RSetUpdatingPauseTimePercent=5",
                "-XX:SurvivorRatio=32",
                "-XX:+PerfDisableSharedMem",
                "-XX:MaxTenuringThreshold=1",
                "-Dusing.aikars.flags=https://mcflags.emc.gs",
                "-Daikars.new.flags=true",
                "-XX:-UseAdaptiveSizePolicy",
                "-XX:CompileThreshold=100",
                "-Dio.netty.recycler.maxCapacity=0",
                "-Dio.netty.recycler.maxCapacity.default=0",
                "-Djline.terminal=jline.UnsupportedTerminal",
                "-Dfile.encoding=UTF-8",
                "-Dclient.encoding.override=UTF-8",
                "-DIReallyKnowWhatIAmDoingISwear=true",
                "-Xmx%memory%M",
                "-jar", "%jarFile%",
                "-p%port%",
                "-h0.0.0.0"
        ));

        new JsonFile(new File("storage/groups/startup/velocity.json")).setList("commands", Arrays.asList(
                "%java%",
                "-XX:+UseG1GC",
                "-XX:G1HeapRegionSize=4M",
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:+ParallelRefProcEnabled",
                "-XX:+AlwaysPreTouch",
                "-XX:MaxInlineLevel=15",
                "-Xmx%memory%M",
                "-jar", "%jarFile%",
                "-p%port%"
        ));
    }
    */

}
