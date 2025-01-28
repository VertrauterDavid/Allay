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

import allay.plugin.proxy.ProxyInstance;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

@SuppressWarnings("unused")
@RequiredArgsConstructor
public class PlayerChooseInitialServerListener {

    private final ProxyInstance instance;
    private final ProxyServer server;

    @Subscribe
    public void handle(PlayerChooseInitialServerEvent event) {
        String lobby = instance.randomLobby();

        server.getServer(lobby).ifPresentOrElse(
                event::setInitialServer,
                () -> event.getPlayer().disconnect(Component.text("§cNo lobby available"))
        );
    }

}
