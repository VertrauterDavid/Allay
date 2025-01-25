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

package allay.master.network;

import allay.api.logger.Logger;
import allay.api.network.NetworkHandlerBase;
import allay.api.network.channel.NetworkChannel;
import allay.api.network.channel.NetworkChannelState;
import allay.api.network.packet.Packet;
import allay.api.network.packet.packets.BroadcastPacket;
import allay.api.network.packet.packets.RedirectToNodePacket;
import allay.api.network.packet.packets.RedirectToServicePacket;
import allay.api.network.packet.packets.service.ServiceAuthPacket;
import allay.api.network.packet.packets.sys.ChannelAuthFailedPacket;
import allay.api.network.packet.packets.sys.ChannelAuthPacket;
import allay.api.network.packet.packets.sys.NodeStatusPacket;
import allay.api.service.CloudService;
import allay.api.service.CloudServiceState;
import allay.master.AllayMaster;
import allay.master.service.ServiceManager;
import io.netty5.channel.Channel;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class NetworkHandler extends NetworkHandlerBase {

    // todo: ip white-/blacklist

    private final AllayMaster allayMaster;
    private final NetworkManager manager;

    @Override
    public NetworkChannel findChannel(Channel channel) {
        return manager.channels().stream().filter(networkChannel -> networkChannel.verifyChannel(channel)).findFirst().orElse(null);
    }

    @Override
    public void onConnect(NetworkChannel networkChannel) {
        allayMaster.logger().debug("[CONNECTED] " + networkChannel.hostname());
        manager.channels().add(networkChannel);
    }

    @Override
    public void onDisconnect(NetworkChannel networkChannel) {
        allayMaster.logger().debug("[DISCONNECTED] " + networkChannel.hostname() + (networkChannel.id() != null ? " - " + networkChannel.id() : ""));
        manager.channels().remove(networkChannel);
        networkChannel.state(NetworkChannelState.CLOSED);
    }

    @Override
    public void onPacket(NetworkChannel networkChannel, Packet packet) {
        if (packet instanceof ChannelAuthPacket authPacket) {
            if (networkChannel.state() == NetworkChannelState.AUTHENTICATION_PENDING) {
                if (!(authPacket.authToken().equals(manager.authToken())) || manager.channel(authPacket.id()) != null) {
                    if (!(authPacket.authToken().equals(manager.authToken()))) {
                        networkChannel.nettyChannel().writeAndFlush(new ChannelAuthFailedPacket(ChannelAuthFailedPacket.REASON_INVALID_AUTH));
                    } else if (manager.channel(authPacket.id()) != null) {
                        networkChannel.nettyChannel().writeAndFlush(new ChannelAuthFailedPacket(ChannelAuthFailedPacket.REASON_INVALID_ID));
                    }

                    networkChannel.state(NetworkChannelState.AUTHENTICATION_DENIED);
                    networkChannel.close();
                    return;
                }
                networkChannel.id(authPacket.id());
                networkChannel.state(NetworkChannelState.AUTHENTICATION_DONE);
                networkChannel.send(new ChannelAuthPacket(authPacket.id(), manager.authToken(), ServiceManager.VELOCITY_SECRET));
                allayMaster.logger().info("Successfully authenticated node §a" + (networkChannel.id() != null ? networkChannel.id() : "unknown") + "§r on §a" + networkChannel.hostname());
            }
            return;
        }

        if (packet instanceof ServiceAuthPacket authPacket) {
            if (networkChannel.state() == NetworkChannelState.AUTHENTICATION_PENDING) {
                CloudService service = allayMaster.serviceManager().service(authPacket.systemId());

                if (service == null) {
                    networkChannel.nettyChannel().writeAndFlush(new ChannelAuthFailedPacket("Service not found"));
                    networkChannel.state(NetworkChannelState.AUTHENTICATION_DENIED);
                    networkChannel.close();
                    return;
                }

                if (!(authPacket.authToken().equals(manager.authToken())) || manager.channel("service-" + authPacket.systemId()) != null) {
                    if (!(authPacket.authToken().equals(manager.authToken()))) {
                        networkChannel.nettyChannel().writeAndFlush(new ChannelAuthFailedPacket(ChannelAuthFailedPacket.REASON_INVALID_AUTH));
                    } else if (manager.channel("service-" + authPacket.systemId()) != null) {
                        networkChannel.nettyChannel().writeAndFlush(new ChannelAuthFailedPacket(ChannelAuthFailedPacket.REASON_INVALID_ID));
                    }

                    networkChannel.state(NetworkChannelState.AUTHENTICATION_DENIED);
                    networkChannel.close();
                    return;
                }

                networkChannel.id("service-" + authPacket.systemId());
                networkChannel.state(NetworkChannelState.AUTHENTICATION_DONE);
                networkChannel.send(packet);

                service.state(CloudServiceState.ONLINE);
                allayMaster.networkManager().channel(service.node()).send(packet);
                allayMaster.logger().info("Successfully authenticated service §a" + service.displayName() + "§r on §a" + networkChannel.hostname());
            }
            return;
        }

        if (Logger.PACKETS) {
            allayMaster.logger().info("[§eRECEIVED§r] " + (networkChannel.id() != null ? networkChannel.id() : "unknown") + " - " + packet.getClass().getSimpleName());
        }

        if (packet instanceof BroadcastPacket broadcastPacket) {
            manager.channels().stream().filter(channel -> channel != networkChannel).forEach(channel -> channel.send(broadcastPacket.targetPacket()));
            return;
        }

        if (packet instanceof RedirectToNodePacket redirectPacket) {
            manager.channel(redirectPacket.receiver()).send(redirectPacket.targetPacket());
            return;
        }

        if (packet instanceof RedirectToServicePacket redirectPacket) {
            manager.channel("service-" + redirectPacket.receiver()).send(redirectPacket.targetPacket());
            return;
        }

        if (packet instanceof NodeStatusPacket statusPacket) {
            networkChannel.state(statusPacket.state());
            networkChannel.send(packet);
            allayMaster.logger().debug("[STATUS] " + (networkChannel.id() != null ? networkChannel.id() : "unknown") + " - " + networkChannel.hostname() + " - " + statusPacket.state());
        }

        if (packet.packetKey() != null && !(packet.packetKey().equalsIgnoreCase(Packet.DEFAULT_PACKET_KEY))) {
            for (NetworkChannel channel : manager.channels()) {
                CompletableFuture<Packet> future = channel.futures().getOrDefault(packet.packetKey(), null);
                if (future != null) {
                    future.complete(packet);
                    return;
                }
            }
        }

        manager.listeners().forEach(listener -> listener.onPacket(packet));
        manager.channels().stream().filter(channel -> channel != networkChannel).forEach(channel -> channel.send(packet));
    }

}
