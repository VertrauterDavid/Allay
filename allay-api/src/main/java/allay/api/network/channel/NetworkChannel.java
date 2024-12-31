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

package allay.api.network.channel;

import allay.api.network.packet.Packet;
import io.netty5.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public class NetworkChannel {

    @Setter
    @Nullable private String id; // any identifier - can also be null
    private final Channel nettyChannel;
    private final HashMap<String, CompletableFuture<Packet>> futures = new HashMap<>();

    @Setter
    private NetworkChannelState state = NetworkChannelState.AUTHENTICATION_PENDING;

    public void send(Packet packet) {
        if (!(nettyChannel.isOpen() && nettyChannel.isActive())) {
            throw new IllegalStateException("Channel is not open or active");
        }

        if (this.state != NetworkChannelState.AUTHENTICATION_DONE && this.state != NetworkChannelState.READY) {
            throw new IllegalStateException("Channel authentication not done yet or channel is already closed");
        }

        this.nettyChannel.writeAndFlush(packet);
    }

    public CompletableFuture<Packet> sendAndReceive(Packet packet) {
        CompletableFuture<Packet> future = new CompletableFuture<>();

        if (this.state != NetworkChannelState.AUTHENTICATION_DONE && this.state != NetworkChannelState.READY) {
            future.completeExceptionally(new IllegalStateException("Channel authentication not done yet or channel is already closed"));
        }

        String key = UUID.randomUUID().toString();
        packet.packetKey(key);

        this.nettyChannel.writeAndFlush(packet);
        this.futures.put(key, future);

        return future;
    }

    public void close() {
        if (this.nettyChannel == null) return;
        this.nettyChannel.close();
    }

    public boolean verifyChannel(Channel channel) {
        return this.nettyChannel.equals(channel);
    }

    public String hostname() {
        return this.nettyChannel.remoteAddress().toString().split(":", -1)[0].substring(1);
    }

}
