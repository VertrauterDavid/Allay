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

import allay.api.network.NetworkComponent;
import allay.api.network.NetworkState;
import allay.api.network.channel.NetworkChannel;
import allay.api.network.channel.NetworkChannelInitializer;
import allay.api.network.packet.Packet;
import allay.api.network.packet.PacketListener;
import allay.api.network.util.NettyUtil;
import allay.master.AllayMaster;
import io.netty5.bootstrap.ServerBootstrap;
import io.netty5.channel.ChannelOption;
import io.netty5.channel.MultithreadEventLoopGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public class NetworkManager extends NetworkComponent {

    private final AllayMaster allayMaster;
    private final String authToken;

    private final CopyOnWriteArrayList<NetworkChannel> channels = new CopyOnWriteArrayList<>();
    private final ArrayList<PacketListener> listeners = new ArrayList<>();

    @Override
    public CompletableFuture<Void> boot() {
        state(NetworkState.CONNECTING);

        CompletableFuture<Void> future = new CompletableFuture<>();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup = new MultithreadEventLoopGroup(NettyUtil.getFactory()), new MultithreadEventLoopGroup(NettyUtil.getFactory()))
                .channelFactory(NettyUtil.getServerChannelFactory())
                .childHandler(new NetworkChannelInitializer(new NetworkHandler(allayMaster, this)))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.IP_TOS, 24)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        bootstrap.bind(config.host(), config.port()).addListener(futureChannel -> {
            if (futureChannel.isSuccess()) {
                state(NetworkState.CONNECTION_ESTABLISHED);
            } else {
                state(NetworkState.CONNECTION_FAILED);
            }
            future.complete(null);
        });

        return future;
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        for (NetworkChannel channel : this.channels) {
            channel.close();
        }
        return super.shutdown();
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

    public NetworkChannel channel(String name) {
        return channels.stream().filter(channel -> (channel.id() != null && channel.id().equals(name)) || channel.hostname().equals(name)).findFirst().orElse(null);
    }

}
