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

import allay.api.network.channel.NetworkChannel;
import allay.api.network.packet.Packet;
import io.netty5.channel.Channel;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.channel.SimpleChannelInboundHandler;
import org.jetbrains.annotations.NotNull;

public abstract class NetworkHandlerBase extends SimpleChannelInboundHandler<Packet> {

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Packet packet) {
        NetworkChannel networkChannel = findChannel(channelHandlerContext.channel());

        if (networkChannel == null){
            channelHandlerContext.close();
            return;
        }

        onPacket(networkChannel, packet);
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext channelHandlerContext)  {
        onConnect(new NetworkChannel(channelHandlerContext.channel()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) {
        onDisconnect(findChannel(channelHandlerContext.channel()));
    }

    @Override
    public void channelExceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
        // we don't care about exceptions lmao
    }

    public abstract NetworkChannel findChannel(Channel channel);

    public abstract void onConnect(NetworkChannel networkChannel);

    public abstract void onDisconnect(NetworkChannel networkChannel);

    public abstract void onPacket(NetworkChannel networkChannel, Packet packet);

}
