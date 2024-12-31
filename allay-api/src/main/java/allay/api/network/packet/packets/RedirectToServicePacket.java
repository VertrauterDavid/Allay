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

package allay.api.network.packet.packets;

import allay.api.network.packet.Packet;
import allay.api.network.packet.PacketBuffer;
import allay.api.network.util.PacketAllocator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
@Getter
public class RedirectToServicePacket extends Packet {

    private UUID receiver;
    private Packet targetPacket;

    @SneakyThrows
    @Override
    public void read(PacketBuffer buffer) {
        receiver = buffer.readUniqueId();
        targetPacket = (Packet) PacketAllocator.allocate(Class.forName(buffer.readString()));
        if (targetPacket != null) {
            targetPacket.read(buffer);
        }
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeUniqueId(receiver);
        buffer.writeString(targetPacket.getClass().getName());
        targetPacket.write(buffer);
    }

}
