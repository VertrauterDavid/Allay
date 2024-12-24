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

import allay.api.network.NetworkHandlerBase;
import allay.api.network.channel.NetworkChannel;
import allay.api.network.channel.NetworkChannelState;
import allay.api.network.packet.Packet;
import allay.api.network.packet.packets.BroadcastPacket;
import allay.api.network.packet.packets.RedirectToNodePacket;
import allay.api.network.packet.packets.RedirectToServicePacket;
import allay.api.network.packet.packets.sys.ChannelAuthPacket;
import allay.master.AllayMaster;
import io.netty5.channel.Channel;
import lombok.RequiredArgsConstructor;

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
        if (networkChannel.state() != NetworkChannelState.READY || packet instanceof ChannelAuthPacket) {
            if (networkChannel.state() == NetworkChannelState.AUTHENTICATION_PENDING && packet instanceof ChannelAuthPacket authPacket) {
                if (!(authPacket.authToken().equals(manager.authToken())) || manager.channel(authPacket.id()) != null) {
                    if (manager.channel(authPacket.id()) != null) {
                        allayMaster.logger().warning("[§cDENIED§r] " + networkChannel.hostname() + " - ID '" + authPacket.id() + "' already in use");
                    }
                    networkChannel.state(NetworkChannelState.AUTHENTICATION_DENIED);
                    networkChannel.close();
                    return;
                }
                networkChannel.id(authPacket.id());
                networkChannel.state(NetworkChannelState.READY);
                networkChannel.send(packet);
                allayMaster.logger().info("[§aVERIFIED§r] " + (networkChannel.id() != null ? networkChannel.id() : "unknown") + " - " + networkChannel.hostname());
            }
            return;
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
            manager.channel("service-" + redirectPacket.receiver()).send(redirectPacket.targetPacket()); // todo: channel name?
            return;
        }

        allayMaster.logger().info("[§eRECEIVED§r] " + (networkChannel.id() != null ? networkChannel.id() : "unknown") + " - " + packet.getClass().getSimpleName());
        manager.listeners().forEach(listener -> listener.onPacket(packet));
        manager.channels().stream().filter(channel -> channel != networkChannel).forEach(channel -> channel.send(packet));
    }

}
