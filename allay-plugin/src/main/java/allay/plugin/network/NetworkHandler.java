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

package allay.plugin.network;

import allay.api.network.NetworkHandlerBase;
import allay.api.network.NetworkState;
import allay.api.network.channel.NetworkChannel;
import allay.api.network.channel.NetworkChannelState;
import allay.api.network.packet.Packet;
import allay.api.network.packet.packets.service.ServiceAuthPacket;
import allay.api.network.packet.packets.sys.ChannelAuthFailedPacket;
import io.netty5.channel.Channel;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class NetworkHandler extends NetworkHandlerBase {

    private final NetworkManager manager;
    private final CompletableFuture<Void> bootFuture;

    @Override
    public NetworkChannel findChannel(Channel channel) {
        return manager.channel();
    }

    @Override
    public void onConnect(NetworkChannel networkChannel) {
        manager.channel(networkChannel);
        networkChannel.nettyChannel().writeAndFlush(new ServiceAuthPacket(manager.config().systemId(), manager.config().authToken()));
    }

    @Override
    public void onDisconnect(NetworkChannel networkChannel) {
        manager.state(NetworkState.CONNECTION_CLOSED);
        networkChannel.state(NetworkChannelState.CLOSED);

        // todo - reconnect if possible
        System.exit(0);
    }

    @Override
    public void onPacket(NetworkChannel networkChannel, Packet packet) {
        if (packet instanceof ServiceAuthPacket authPacket) {
            if (networkChannel.state() == NetworkChannelState.AUTHENTICATION_PENDING) {
                if (!(authPacket.authToken().equals(manager.config().authToken()))) return;
                networkChannel.id("service-" + authPacket.systemId());
                networkChannel.state(NetworkChannelState.AUTHENTICATION_DONE);
                bootFuture.complete(null);
            }
            return;
        }

        if (packet instanceof ChannelAuthFailedPacket authPacket) {
            System.out.println(" ");
            System.out.println("§cAuthentication to master failed");
            System.out.println("§cReason§r: " + authPacket.reason());
            System.out.println(" ");

            System.exit(0);
            return;
        }

        if (packet.packetKey() != null && !(packet.packetKey().equalsIgnoreCase(Packet.DEFAULT_PACKET_KEY))) {
            CompletableFuture<Packet> future = manager.channel().futures().getOrDefault(packet.packetKey(), null);
            if (future != null) {
                future.complete(packet);
                return;
            }
        }

        manager.listeners().forEach(listener -> listener.onPacket(packet));
    }

}
