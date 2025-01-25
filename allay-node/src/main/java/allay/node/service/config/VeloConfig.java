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
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class VeloConfig {

    public static void setup1(RunningService runningService) throws IOException {
        File configFile = new File(runningService.directory(), "velocity.toml");
        if (!configFile.exists()) configFile.createNewFile();

        Toml toml = new Toml();
        Map<String, Object> data = configFile.exists() && configFile.length() > 0 ? toml.read(new FileReader(configFile)).toMap() : new HashMap<>();

        data.put("bind", "0.0.0.0:" + runningService.service().port());
        data.put("motd", "Allay");
        data.put("online-mode", true);
        data.put("force-key-authentication", true);
        data.put("player-info-forwarding-mode", "modern");
        data.put("enable-player-address-logging", false);
        data.put("show-max-players", 25000);

        Map<String, Object> advanced = new HashMap<>();
        advanced.put("haproxy-protocol", true);
        advanced.put("announce-proxy-commands", true);
        advanced.put("log-command-executions", false);
        advanced.put("log-player-connections", false);
        data.put("advanced", advanced);

        TomlWriter writer = new TomlWriter();
        writer.write(data, configFile);
    }

    public static void setup2(RunningService runningService) throws IOException {
        File secretFile = new File(runningService.directory(), "forwarding.secret");
        try (FileWriter writer = new FileWriter(secretFile)) {
            writer.write(ServiceManager.VELOCITY_SECRET);
        }
    }

}
