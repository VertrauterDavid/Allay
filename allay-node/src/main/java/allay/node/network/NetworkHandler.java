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

package allay.node.network;

import allay.api.logger.Logger;
import allay.api.network.NetworkHandlerBase;
import allay.api.network.NetworkState;
import allay.api.network.channel.NetworkChannel;
import allay.api.network.channel.NetworkChannelState;
import allay.api.network.packet.Packet;
import allay.api.network.packet.packets.sys.ChannelAuthFailedPacket;
import allay.api.network.packet.packets.sys.ChannelAuthPacket;
import allay.api.network.packet.packets.sys.NodeStatusPacket;
import allay.node.AllayNode;
import io.netty5.channel.Channel;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class NetworkHandler extends NetworkHandlerBase {

    // todo: auto reconnect on master restart

    private final AllayNode allayNode;
    private final NetworkManager manager;
    private final CompletableFuture<Void> bootFuture;

    @Override
    public NetworkChannel findChannel(Channel channel) {
        return manager.channel();
    }

    @Override
    public void onConnect(NetworkChannel networkChannel) {
        manager.channel(networkChannel);
        networkChannel.nettyChannel().writeAndFlush(new ChannelAuthPacket(manager.id(), manager.authToken()));
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
        if (packet instanceof ChannelAuthPacket authPacket) {
            if (networkChannel.state() == NetworkChannelState.AUTHENTICATION_PENDING) {
                if (!(authPacket.authToken().equals(manager.authToken()))) return;
                networkChannel.id(authPacket.id());
                networkChannel.state(NetworkChannelState.AUTHENTICATION_DONE);
                bootFuture.complete(null);
                allayNode.logger().info("[§eVERIFIED§r] Successfully authenticated with §a" + networkChannel.hostname());
            }
            return;
        }

        if (packet instanceof ChannelAuthFailedPacket authPacket) {
            allayNode.logger().warning(" ");
            allayNode.logger().warning("§cAuthentication to master failed");
            allayNode.logger().warning("§cReason§r: " + authPacket.reason());
            switch (authPacket.reason()) {
                case ChannelAuthFailedPacket.REASON_INVALID_AUTH -> {
                    allayNode.logger().warning(" ");
                    allayNode.logger().warning("§cCheck the auth-token in §cstorage/config/netty.json");
                }
                case ChannelAuthFailedPacket.REASON_INVALID_ID -> {
                    allayNode.logger().warning(" ");
                    allayNode.logger().warning("§cCheck the id in §cstorage/config/netty.json");
                }
            }
            allayNode.logger().warning(" ");

            allayNode.skipShutdownHook(true);
            allayNode.sleep(2000);

            System.exit(0);
            return;
        }

        if (Logger.PACKETS) {
            allayNode.logger().info("[§eRECEIVED§r] " + packet.getClass().getSimpleName());
        }

        if (packet instanceof NodeStatusPacket statusPacket) {
            if (networkChannel.state() == NetworkChannelState.AUTHENTICATION_DONE && statusPacket.state() == NetworkChannelState.READY) {
                networkChannel.state(statusPacket.state());
                bootFuture.complete(null);
            }
        }

        if (packet.packetKey() != null && !(packet.packetKey().equalsIgnoreCase(Packet.DEFAULT_PACKET_KEY))) {
            CompletableFuture<Packet> future = allayNode.networkManager().channel().futures().getOrDefault(packet.packetKey(), null);
            if (future != null) {
                future.complete(packet);
                return;
            }
        }

        manager.listeners().forEach(listener -> listener.onPacket(packet));
    }

}
