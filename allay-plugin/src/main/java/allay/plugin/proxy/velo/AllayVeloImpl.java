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

import allay.api.service.CloudService;
import allay.plugin.proxy.ProxyInstance;
import allay.plugin.proxy.velo.listener.DisconnectListener;
import allay.plugin.proxy.velo.listener.PlayerChooseInitialServerListener;
import allay.plugin.proxy.velo.listener.ProxyPingListener;
import allay.plugin.proxy.velo.listener.ServerConnectedListener;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;

import java.net.InetSocketAddress;
import java.util.UUID;

@RequiredArgsConstructor
public class AllayVeloImpl extends ProxyInstance {

    private final AllayVelo instance;
    private final ProxyServer server;

    @Override
    public void enable() {
        super.enable();

        server.getAllServers().forEach(registeredServer -> server.unregisterServer(registeredServer.getServerInfo()));
        server.getEventManager().register(instance, new DisconnectListener());
        server.getEventManager().register(instance, new PlayerChooseInitialServerListener(this, server));
        server.getEventManager().register(instance, new ProxyPingListener());
        server.getEventManager().register(instance, new ServerConnectedListener());
    }

    @Override
    public void registerServer(CloudService service) {
        ServerInfo serverInfo = new ServerInfo(service.displayName(), InetSocketAddress.createUnresolved(service.ip(), service.port()));
        server.registerServer(serverInfo);

        if (service.displayName().toLowerCase().contains("lobby")) {
            defaultServers.add(service.displayName());
        }
    }

    @Override
    public void unregisterServer(CloudService service) {
        server.getServer(service.displayName()).ifPresent(registeredServer -> server.unregisterServer(registeredServer.getServerInfo()));
        defaultServers.remove(service.displayName());
    }

    @Override
    public void execute(String command) {
        server.getCommandManager().executeAsync(server.getConsoleCommandSource(), command);
    }

    @Override
    public void message(UUID uuid, String message) {
        server.getPlayer(uuid).ifPresent(player -> player.sendMessage(Component.text(message)));
    }

    @Override
    public void title(UUID uuid, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        server.getPlayer(uuid).ifPresent(player -> player.showTitle(Title.title(Component.text(title), Component.text(subTitle), Title.Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut)))));
    }

    @Override
    public void actionbar(UUID uuid, String message) {
        server.getPlayer(uuid).ifPresent(player -> player.sendActionBar(Component.text(message)));
    }

    @Override
    public void connect(UUID uuid, String serviceName) {
        server.getServer(serviceName).ifPresent(registeredServer -> server.getPlayer(uuid).ifPresent(player -> player.createConnectionRequest(registeredServer).connect()));
    }

}
