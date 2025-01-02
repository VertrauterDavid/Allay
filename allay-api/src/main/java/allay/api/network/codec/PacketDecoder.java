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

package allay.api.network.codec;

import allay.api.logger.Logger;
import allay.api.network.packet.Packet;
import allay.api.network.packet.PacketBuffer;
import allay.api.network.util.PacketAllocator;
import io.netty5.buffer.Buffer;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.handler.codec.ByteToMessageDecoder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class PacketDecoder extends ByteToMessageDecoder {

    @Nullable
    private final Logger logger;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Buffer in) {
        PacketBuffer buffer = new PacketBuffer(in);

        if (in.readableBytes() < Integer.BYTES) {
            if (logger != null) {
                logger.error("Insufficient data to decode packet class name length.");
            }
            return;
        }

        String className = buffer.readString();

        try {
            if (in.readableBytes() < Integer.BYTES) {
                if (logger != null) {
                    logger.error("Insufficient data to read the packet payload size.");
                }
                return;
            }

            int readableBytes = buffer.readInt();
            if (in.readableBytes() < readableBytes) {
                if (logger != null) {
                    logger.error("Insufficient data to read the packet payload. Expected: " + readableBytes + ", Available: " + in.readableBytes());
                }
                return;
            }

            PacketBuffer content = new PacketBuffer(in.copy(in.readerOffset(), readableBytes, true));
            in.skipReadableBytes(readableBytes);

            Packet packet = (Packet) PacketAllocator.allocate(Class.forName(className));
            if (packet != null) {
                packet.packetKey(content.readString());
                packet.read(content);
            }

            if (logger != null) {
                logger.debug("Decoding packet: " + className + ", readable bytes: " + readableBytes);
            }

            buffer.resetBuffer();
            channelHandlerContext.fireChannelRead(packet);
        } catch (Exception exception) {
            if (logger != null) {
                logger.exception(exception);
            }
        }
    }

}
