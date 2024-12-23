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

package allay.api.network;

import io.netty5.channel.MultithreadEventLoopGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Accessors(fluent = true)
@Setter
public abstract class NetworkComponent {

    @Getter
    private NetworkState state = NetworkState.INITIALIZING;

    protected NetworkConfig config = new NetworkConfig();
    protected MultithreadEventLoopGroup bossGroup;

    public abstract CompletableFuture<Void> boot();

    public CompletableFuture<Void> shutdown() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        this.bossGroup.shutdownGracefully().addListener(v -> {
            this.state = NetworkState.CONNECTION_CLOSED;
            future.complete(null);
        });

        return future;
    }

    @SneakyThrows
    public void bootSync() {
        boot().get(5, TimeUnit.SECONDS);
    }

    @SneakyThrows
    public void shutdownSync() {
        shutdown().get(5, TimeUnit.SECONDS);
    }

}
