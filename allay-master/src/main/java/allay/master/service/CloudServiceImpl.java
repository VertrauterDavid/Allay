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

import allay.api.network.channel.NetworkChannel;
import allay.api.network.packet.Packet;
import allay.api.player.CloudPlayer;
import allay.api.service.CloudGroup;
import allay.api.service.CloudService;
import allay.api.service.CloudServiceState;
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
public class CloudServiceImpl implements CloudService {

    private final CloudGroup group;
    private final CloudServiceState state;

    private final UUID systemId;
    private final int orderId;

    private final String hostname;
    private final String ip;
    private final int port;

    private NetworkChannel channel = null;

    @Override
    public void shutdown() {
        // todo: send action packet to shutdown
    }

    @Override
    public void execute(String command) {
        // todo: send action packet with command
    }

    @Override
    public CompletableFuture<Integer> onlinePlayerCount() {
        // todo: send request packet and wait for response
        return null;
    }

    @Override
    public CompletableFuture<CloudPlayer> onlinePlayers() {
        // todo: send request packet and wait for response
        return null;
    }

    @Override
    public void sendPacket(Packet packet) {
        if (channel == null) throw new IllegalStateException("Channel is not initialized yet - wait for the connection to be established");
        channel.send(packet);
    }

}
