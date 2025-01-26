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
import allay.api.network.util.NetworkUtil;
import allay.api.util.JsonFile;
import allay.node.AllayNode;
import allay.node.web.WebManager;
import io.netty5.bootstrap.Bootstrap;
import io.netty5.channel.ChannelOption;
import io.netty5.channel.MultithreadEventLoopGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Accessors(fluent = true)
@RequiredArgsConstructor
@Getter
public class NetworkManager extends NetworkComponent {

    private final AllayNode allayNode;
    private final String id;
    private final String authToken;
    private final String host;
    private final int port;

    @Setter
    private NetworkChannel channel;
    private final ArrayList<PacketListener> listeners = new ArrayList<>();

    public NetworkManager(AllayNode allayNode) {
        this.allayNode = allayNode;

        JsonFile config = new JsonFile(new File("storage/config/netty.json"))
                .setStringDefault("id", "Node-" + UUID.randomUUID().toString().split("-")[0])
                .setStringDefault("authToken", "put-master-auth-token-here")
                .setStringDefault("host", "0.0.0.0")
                .setLongDefault("port", 8040)
                .setLongDefault("web-port", 8050);

        this.id = config.getString("id");
        this.authToken = config.getString("authToken");
        this.host = config.getString("host");
        this.port = (int) config.getLong("port");

        new WebManager(allayNode, (int) config.getLong("web-port")).boot().join();
    }

    @Override
    public CompletableFuture<Void> boot() {
        state(NetworkState.CONNECTING);

        // just caching the ip here - so we don't have to wait for the response later
        NetworkUtil.getCurrentIp();

        CompletableFuture<Void> future = new CompletableFuture<>();
        Bootstrap bootstrap = new Bootstrap()
                .group(bossGroup = new MultithreadEventLoopGroup(NetworkUtil.getFactory()))
                .channelFactory(NetworkUtil.getSocketChannelFactory())
                .handler(new NetworkChannelInitializer(allayNode.logger(), new NetworkHandler(allayNode, this, future)))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.IP_TOS, 24);

        bootstrap.connect(this.host, this.port).addListener(futureChannel -> {
            if (futureChannel.isSuccess()) {
                state(NetworkState.CONNECTION_ESTABLISHED);
            } else {
                state(NetworkState.CONNECTION_FAILED);

                allayNode.logger().warning(" ");
                allayNode.logger().warning("§cFailed to connect to master");
                allayNode.logger().warning("§cCheck the host and port in §cstorage/config/netty.json");
                allayNode.logger().warning(" ");
                allayNode.logger().warning("§cReason§r: " + futureChannel.cause().getLocalizedMessage());
                allayNode.logger().warning(" ");

                allayNode.skipShutdownHook(true);
                allayNode.sleep(2000);

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
