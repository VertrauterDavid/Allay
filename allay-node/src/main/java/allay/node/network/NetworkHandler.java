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

import allay.api.network.NetworkHandlerBase;
import allay.api.network.NetworkState;
import allay.api.network.channel.NetworkChannel;
import allay.api.network.channel.NetworkChannelState;
import allay.api.network.packet.Packet;
import allay.api.network.packet.packets.sys.ChannelAuthPacket;
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
        manager.channel(null);
        manager.state(NetworkState.CONNECTION_CLOSED);
        networkChannel.state(NetworkChannelState.CLOSED);
    }

    @Override
    public void onPacket(NetworkChannel networkChannel, Packet packet) {
        if (networkChannel.state() != NetworkChannelState.READY || packet instanceof ChannelAuthPacket) {
            if (networkChannel.state() == NetworkChannelState.AUTHENTICATION_PENDING && packet instanceof ChannelAuthPacket authPacket) {
                if (!(authPacket.authToken().equals(manager.authToken()))) return;
                networkChannel.id(authPacket.id());
                networkChannel.state(NetworkChannelState.READY);
                bootFuture.complete(null);
            }
            return;
        }

        manager.listeners().forEach(listener -> listener.onPacket(packet));
    }

}
