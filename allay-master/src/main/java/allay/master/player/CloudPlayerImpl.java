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

package allay.master.player;

import allay.api.network.packet.packets.player.PlayerConnectPacket;
import allay.api.network.packet.packets.player.PlayerMessagePacket;
import allay.api.network.packet.packets.player.PlayerTitlePacket;
import allay.api.player.CloudPlayer;
import allay.api.service.CloudService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
@Setter
public class CloudPlayerImpl implements CloudPlayer {

    private final String name;
    private final UUID uniqueId;

    private String server;
    private String proxy;

    @Override
    public CompletableFuture<CloudService> currentServer() {
        return null; // todo: return instance of CloudService - CompletableFuture#completedFuture()
    }

    @Override
    public CompletableFuture<CloudService> currentProxy() {
        return null; // todo: return instance of CloudService - CompletableFuture#completedFuture()
    }

    @Override
    public void sendMessage(String message) {
        currentProxy().thenAccept(service -> service.sendPacket(new PlayerMessagePacket(uniqueId, message, PlayerMessagePacket.Type.MESSAGE)));
    }

    @Override
    public void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        currentProxy().thenAccept(service -> service.sendPacket(new PlayerTitlePacket(uniqueId, title, subTitle, fadeIn, stay, fadeOut)));
    }

    @Override
    public void sendActionbar(String message) {
        currentProxy().thenAccept(service -> service.sendPacket(new PlayerMessagePacket(uniqueId, message, PlayerMessagePacket.Type.ACTIONBAR)));
    }

    @Override
    public void connect(String serviceName) {
        currentProxy().thenAccept(service -> service.sendPacket(new PlayerConnectPacket(uniqueId, serviceName)));
    }

}
