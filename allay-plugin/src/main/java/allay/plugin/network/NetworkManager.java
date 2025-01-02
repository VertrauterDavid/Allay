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

import allay.api.network.NetworkComponent;
import allay.api.network.NetworkState;
import allay.api.network.channel.NetworkChannel;
import allay.api.network.channel.NetworkChannelInitializer;
import allay.api.network.packet.Packet;
import allay.api.network.packet.PacketListener;
import allay.api.network.util.NetworkUtil;
import io.netty5.bootstrap.Bootstrap;
import io.netty5.channel.ChannelOption;
import io.netty5.channel.MultithreadEventLoopGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public class NetworkManager extends NetworkComponent {

    private final NetworkConfig config;

    @Setter
    private NetworkChannel channel;
    private final ArrayList<PacketListener> listeners = new ArrayList<>();

    @Override
    public CompletableFuture<Void> boot() {
        state(NetworkState.CONNECTING);

        // just caching the ip here - so we don't have to wait for the response later
        NetworkUtil.getCurrentIp();

        CompletableFuture<Void> future = new CompletableFuture<>();
        Bootstrap bootstrap = new Bootstrap()
                .group(bossGroup = new MultithreadEventLoopGroup(NetworkUtil.getFactory()))
                .channelFactory(NetworkUtil.getSocketChannelFactory())
                .handler(new NetworkChannelInitializer(null, new NetworkHandler(this, future)))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.IP_TOS, 24);

        bootstrap.connect(config.host(), config.port()).addListener(futureChannel -> {
            if (futureChannel.isSuccess()) {
                state(NetworkState.CONNECTION_ESTABLISHED);
            } else {
                state(NetworkState.CONNECTION_FAILED);

                System.out.println(" ");
                System.out.println("§cFailed to connect to master");
                System.out.println("§cCheck the host and port in §cstorage/config/netty.json");
                System.out.println(" ");

                System.exit(0);
            }
        });

        return future;
    }

    public <P extends Packet> void addListener(Class<P> clazz, Consumer<P> listener) {
        listeners.add(packet -> {
            if (clazz.isInstance(packet)) {
                listener.accept(clazz.cast(packet));
            }
        });
    }

}
