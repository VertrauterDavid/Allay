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

import allay.api.network.packet.Packet;
import allay.api.network.packet.PacketBuffer;
import io.netty5.buffer.Buffer;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.handler.codec.MessageToByteEncoder;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class PacketEncoder extends MessageToByteEncoder<Packet> {

    private static final HashMap<Packet, PacketBuffer> tempPacketEncoderList = new HashMap<>();

    @Override
    protected Buffer allocateBuffer(ChannelHandlerContext channelHandlerContext, @NotNull Packet packet) {
        try {
            PacketBuffer buffer = PacketBuffer.allocate();
            packet.write(buffer);
            tempPacketEncoderList.put(packet, buffer);

            // amount of chars in class name
            int bytes = Integer.BYTES +
                    // class name
                    packet.getClass().getName().getBytes(StandardCharsets.UTF_8).length +
                    // amount of bytes in buffer
                    Integer.BYTES +
                    // buffer content
                    buffer.origin().readableBytes();

            return channelHandlerContext.bufferAllocator().allocate(bytes);
        } catch (Exception ignored) { }

        return null;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, Buffer out) {
        try {
            var origin = tempPacketEncoderList.get(packet).origin();
            var buffer = new PacketBuffer(out);
            var readableBytes = origin.readableBytes();

            buffer.writeString(packet.getClass().getName());
            buffer.writeInt(readableBytes);

            origin.copyInto(0, out, out.writerOffset(), readableBytes);
            out.skipWritableBytes(readableBytes);
        } catch (Exception ignored) { }

        tempPacketEncoderList.remove(packet);
    }
}
