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

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public class NetworkChannel {

    @Setter
    @Nullable private String id; // any identifier - can also be null
    private final Channel nettyChannel;

    @Setter
    private NetworkChannelState state = NetworkChannelState.AUTHENTICATION_PENDING;

    public void send(Packet packet) {
        if (this.state == NetworkChannelState.AUTHENTICATION_PENDING) throw new IllegalStateException("Channel is not authenticated");
        if (this.state == NetworkChannelState.CLOSED) throw new IllegalStateException("Channel is closed");
        this.nettyChannel.writeAndFlush(packet);
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
