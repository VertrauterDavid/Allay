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
import io.netty5.buffer.Buffer;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@RequiredArgsConstructor
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Nullable
    private final Logger logger;
    private static final HashMap<Packet, PacketBuffer> tempPacketEncoderList = new HashMap<>();

    @Override
    protected Buffer allocateBuffer(ChannelHandlerContext channelHandlerContext, @NotNull Packet packet) {
        try {
            PacketBuffer buffer = PacketBuffer.allocate();
            buffer.writeString((packet.packetKey() == null ? Packet.DEFAULT_PACKET_KEY : packet.packetKey()));
            packet.write(buffer);

            if (tempPacketEncoderList.put(packet, buffer) != null) {
                if (logger != null) {
                    logger.warning("Replacing existing buffer for packet: " + packet.getClass().getName());
                }
            }

            // amount of chars in class name
            int bytes = Integer.BYTES +
                    // class name
                    packet.getClass().getName().getBytes(StandardCharsets.UTF_8).length +
                    // amount of bytes in buffer
                    Integer.BYTES +
                    // buffer content
                    buffer.origin().readableBytes();

            return channelHandlerContext.bufferAllocator().allocate(bytes);
        } catch (Exception exception) {
            if (logger != null) {
                logger.exception(exception);
            }
        }

        return null;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, Buffer out) {
        try {
            var tempBuffer = tempPacketEncoderList.get(packet);
            if (tempBuffer == null) {
                if (logger != null) {
                    logger.error("Buffer not found for packet: " + packet.getClass().getName());
                }
                return;
            }

            var origin = tempBuffer.origin();
            if (origin.readableBytes() == 0) {
                if (logger != null) {
                    logger.warning("No readable bytes found for packet: " + packet.getClass().getName());
                }
                return;
            }

            var buffer = new PacketBuffer(out);
            var readableBytes = origin.readableBytes();

            buffer.writeString(packet.getClass().getName());
            buffer.writeInt(readableBytes);

            if (logger != null) {
                logger.debug("Encoding packet: " + packet.getClass().getName() + ", readable bytes: " + readableBytes);
            }

            origin.copyInto(0, out, out.writerOffset(), readableBytes);
            out.skipWritableBytes(readableBytes);
        } catch (Exception exception) {
            if (logger != null) {
                logger.exception(exception);
            }
        } finally {
            tempPacketEncoderList.remove(packet);
        }
    }

}
