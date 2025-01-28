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

package allay.node.service.config;

import allay.node.service.RunningService;
import allay.node.service.ServiceManager;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

@SuppressWarnings("all")
public class BukkitConfig {

    public static void setup1(RunningService runningService) throws IOException {
        File configFile = new File(runningService.directory(), "server.properties");
        Properties properties = new Properties();
        if (configFile.exists()) properties.load(new FileInputStream(configFile));

        properties.setProperty("online-mode", "false");
        properties.setProperty("server-port", String.valueOf(runningService.service().port()));
        // properties.setProperty("server-ip", "0.0.0.0");
        properties.setProperty("server-name", runningService.service().displayName());
        properties.setProperty("motd", runningService.service().displayName());
        properties.setProperty("enforce-secure-profile", "false");
        properties.setProperty("max-players", "25000");
        properties.store(new FileOutputStream(configFile), "Server Properties");
    }

    public static void setup2(RunningService runningService) throws IOException {
        File configFile = new File(runningService.directory(), "spigot.yml");
        if (!configFile.exists()) configFile.createNewFile();

        Yaml yaml = new Yaml();
        Map<String, Object> data = configFile.exists() && configFile.length() > 0 ? yaml.load(new FileInputStream(configFile)) : new HashMap<>();
        Map<String, Object> settings = (Map<String, Object>) data.computeIfAbsent("settings", k -> new HashMap<>());
        settings.put("restart-on-crash", false);
        settings.put("bungeecord", false);

        yaml = new Yaml(new DumperOptions() {{ setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); }});
        yaml.dump(data, new FileWriter(configFile));
    }

    public static void setupPaperVelo(RunningService runningService) throws IOException {
        File configFile = new File(runningService.directory() + "/config", "paper-global.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
        }

        Yaml yaml = new Yaml();
        Map<String, Object> data = configFile.exists() && configFile.length() > 0 ? yaml.load(new FileInputStream(configFile)) : new HashMap<>();
        Map<String, Object> proxies = (Map<String, Object>) data.computeIfAbsent("proxies", k -> new HashMap<>());

        Map<String, Object> bungeeCord = (Map<String, Object>) proxies.computeIfAbsent("bungee-cord", k -> new HashMap<>());
        bungeeCord.put("online-mode", false);

        proxies.put("proxy-protocol", false);

        Map<String, Object> velocity = (Map<String, Object>) proxies.computeIfAbsent("velocity", k -> new HashMap<>());
        velocity.put("enabled", true);
        velocity.put("online-mode", true);
        velocity.put("secret", ServiceManager.VELOCITY_SECRET);

        yaml = new Yaml(new DumperOptions() {{ setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); }});
        yaml.dump(data, new FileWriter(configFile));
    }

}
