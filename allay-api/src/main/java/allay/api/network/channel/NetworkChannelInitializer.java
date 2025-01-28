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

import allay.api.logger.Logger;
import allay.api.network.NetworkHandlerBase;
import allay.api.network.codec.PacketDecoder;
import allay.api.network.codec.PacketEncoder;
import io.netty5.channel.Channel;
import io.netty5.channel.ChannelInitializer;
import io.netty5.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty5.handler.codec.LengthFieldPrepender;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class NetworkChannelInitializer extends ChannelInitializer<Channel> {

    @Nullable
    private final Logger logger;
    private final NetworkHandlerBase networkHandler;

    @Override
    protected void initChannel(Channel channel) {
        channel.pipeline()
                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, Integer.BYTES, 0, Integer.BYTES))
                .addLast(new PacketDecoder(logger))
                .addLast(new LengthFieldPrepender(Integer.BYTES))
                .addLast(new PacketEncoder(logger))
                .addLast(networkHandler);
    }

}
