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

package allay.plugin.proxy.velo.listener;

import allay.plugin.util.ColorUtil;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;

import java.util.UUID;

public class ProxyPingListener {

    @Subscribe
    public void handle(ProxyPingEvent event) {
        ServerPing serverPing = event.getPing();
        ServerPing.Builder builder = serverPing.asBuilder();

        builder.onlinePlayers(100);
        builder.maximumPlayers(19000);
        builder.description(ColorUtil.translateColorCodes(
                "              &#559EFF&lT&#52AEFF&lr&#4EBEFF&ly&#4BCEFF&lS&#47DEFF&lm&#44EEFF&lp &8- &7Maintenance\n" +
                "&7"
        ));
        builder.clearSamplePlayers();
        builder.samplePlayers(new ServerPing.SamplePlayer("§7" + System.getenv("ALLAY_SERVICE_NAME"), UUID.randomUUID()));

        event.setPing(builder.build());
    }

}
