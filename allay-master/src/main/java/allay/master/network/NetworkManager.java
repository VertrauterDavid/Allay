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
import allay.api.network.util.NetworkUtil;
import allay.api.util.JsonFile;
import allay.api.util.StringUtil;
import allay.master.AllayMaster;
import io.netty5.bootstrap.ServerBootstrap;
import io.netty5.channel.ChannelOption;
import io.netty5.channel.MultithreadEventLoopGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public class NetworkManager extends NetworkComponent {

    private final AllayMaster allayMaster;
    private final String authToken;
    private final String host;
    private final int port;

    private final CopyOnWriteArrayList<NetworkChannel> channels = new CopyOnWriteArrayList<>();
    private final ArrayList<PacketListener> listeners = new ArrayList<>();

    public NetworkManager(AllayMaster allayMaster) {
        this.allayMaster = allayMaster;

        JsonFile config = new JsonFile(new File("storage/config/netty.json"))
                .setStringDefault("authToken", StringUtil.generateRandom(32))
                .setStringDefault("host", "0.0.0.0")
                .setLongDefault("port", 8040);

        this.authToken = config.getString("authToken");
        this.host = config.getString("host");
        this.port = (int) config.getLong("port");
    }

    @Override
    public CompletableFuture<Void> boot() {
        state(NetworkState.CONNECTING);

        // just caching the ip here - so we don't have to wait for the response later
        NetworkUtil.getCurrentIp();

        CompletableFuture<Void> future = new CompletableFuture<>();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup = new MultithreadEventLoopGroup(NetworkUtil.getFactory()), new MultithreadEventLoopGroup(NetworkUtil.getFactory()))
                .channelFactory(NetworkUtil.getServerChannelFactory())
                .childHandler(new NetworkChannelInitializer(allayMaster.logger(), new NetworkHandler(allayMaster, this)))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.IP_TOS, 24);

        bootstrap.bind(this.host, this.port).addListener(futureChannel -> {
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

    public <P extends Packet> void addListener(Class<P> clazz, Consumer<P> listener) {
        listeners.add(packet -> {
            if (clazz.isInstance(packet)) {
                listener.accept(clazz.cast(packet));
            }
        });
    }

    public NetworkChannel channel(String name) {
        return channels.stream().filter(channel -> (channel.id() != null && channel.id().equals(name)) || channel.hostname().equals(name)).findFirst().orElse(null);
    }

}
