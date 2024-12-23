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

import allay.api.network.NetworkComponent;
import allay.api.network.NetworkState;
import allay.api.network.channel.NetworkChannel;
import allay.api.network.channel.NetworkChannelInitializer;
import allay.api.network.packet.Packet;
import allay.api.network.packet.PacketListener;
import allay.api.network.util.NettyUtil;
import allay.node.AllayNode;
import io.netty5.bootstrap.Bootstrap;
import io.netty5.channel.ChannelOption;
import io.netty5.channel.MultithreadEventLoopGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Accessors(fluent = true)
@RequiredArgsConstructor
@Getter
public class NetworkManager extends NetworkComponent {

    private final AllayNode allayNode;
    private final String id;
    private final String authToken;

    @Setter
    private NetworkChannel channel;
    private final ArrayList<PacketListener> listeners = new ArrayList<>();

    @Override
    public CompletableFuture<Void> boot() {
        state(NetworkState.CONNECTING);

        CompletableFuture<Void> future = new CompletableFuture<>();
        Bootstrap bootstrap = new Bootstrap()
                .group(bossGroup = new MultithreadEventLoopGroup(NettyUtil.getFactory()))
                .channelFactory(NettyUtil.getSocketChannelFactory())
                .handler(new NetworkChannelInitializer(new NetworkHandler(allayNode, this, future)))
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.IP_TOS, 24)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);

        bootstrap.connect(config.host(), config.port()).addListener(futureChannel -> {
            if (futureChannel.isSuccess()) {
                state(NetworkState.CONNECTION_ESTABLISHED);
            } else {
                state(NetworkState.CONNECTION_FAILED);
                future.complete(null);
            }
        });

        return future;
    }

    public void addListener(PacketListener listener) {
        listeners.add(listener);
    }

    public void addListener(Class<? extends Packet> clazz, PacketListener listener) {
        listeners.add(packet -> {
            if (clazz.isInstance(packet)) {
                listener.onPacket(packet);
            }
        });
    }

}
