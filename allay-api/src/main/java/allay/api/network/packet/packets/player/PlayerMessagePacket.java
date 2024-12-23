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

package allay.api.network.packet.packets.player;

import allay.api.network.packet.Packet;
import allay.api.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
@Getter
public class PlayerMessagePacket extends Packet {

    private UUID uuid;
    private String message;
    private Type type;

    @Override
    public void read(PacketBuffer buffer) {
        uuid = buffer.readUniqueId();
        message = buffer.readString();
        type = buffer.readEnum(Type.class);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeUniqueId(uuid);
        buffer.writeString(message);
        buffer.writeEnum(type);
    }

    public enum Type {
        MESSAGE,
        ACTIONBAR
    }

}