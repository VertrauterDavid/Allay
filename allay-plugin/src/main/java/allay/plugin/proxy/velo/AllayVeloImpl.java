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

package allay.plugin.proxy.velo;

import allay.plugin.proxy.ProxyInstance;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.connection.Player;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import net.kyori.adventure.util.Ticks;

import java.util.UUID;

@RequiredArgsConstructor
public class AllayVeloImpl extends ProxyInstance {

    private final ProxyServer server;

    @Override
    public void message(UUID uuid, String message) {
        player(uuid).sendMessage(Component.text(message));
    }

    @Override
    public void title(UUID uuid, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        player(uuid).sendTitlePart(TitlePart.TITLE, Component.text(title));
        player(uuid).sendTitlePart(TitlePart.SUBTITLE, Component.text(subTitle));
        player(uuid).sendTitlePart(TitlePart.TIMES, Title.Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut)));
    }

    @Override
    public void actionbar(UUID uuid, String message) {
        player(uuid).sendActionBar(Component.text(message));
    }

    @Override
    public void connect(UUID uuid, String serviceName) {
        player(uuid).createConnectionRequest(server.server(serviceName)).fireAndForget();
    }

    private Player player(UUID uuid) {
        return server.player(uuid);
    }

}
