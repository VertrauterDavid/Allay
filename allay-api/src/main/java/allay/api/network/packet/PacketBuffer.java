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

package allay.api.network.packet;

import io.netty5.buffer.Buffer;
import io.netty5.buffer.BufferAllocator;
import io.netty5.buffer.DefaultBufferAllocators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.charset.StandardCharsets;
import java.util.*;

@AllArgsConstructor
@Accessors(fluent = true)
@SuppressWarnings("all")
public class PacketBuffer {

    private static final BufferAllocator BUFFER_ALLOCATOR = DefaultBufferAllocators.offHeapAllocator();

    @Getter
    private final Buffer origin;

    public static PacketBuffer allocate() {
        return allocate(0);
    }

    public static PacketBuffer allocate(int i) {
        return new PacketBuffer(BUFFER_ALLOCATOR.allocate(i));
    }

    public void resetBuffer() {
        if (origin.readableBytes() > 0) {
            origin.skipReadableBytes(origin.readableBytes());
        }
    }

    public PacketBuffer writeString(String value) {
        var bytes = value.getBytes(StandardCharsets.UTF_8);
        this.origin.writeInt(bytes.length);
        this.origin.writeBytes(bytes);
        return this;
    }

    public String readString() {
        return this.origin.readCharSequence(this.origin.readInt(), StandardCharsets.UTF_8).toString();
    }

    public PacketBuffer writeBoolean(Boolean booleanValue) {
        this.origin.writeBoolean(booleanValue);
        return this;
    }

    public boolean readBoolean() {
        return this.origin.readBoolean();
    }

    public PacketBuffer writeInt(int value) {
        this.origin.writeInt(value);
        return this;
    }

    public int readInt() {
        return this.origin.readInt();
    }

    public PacketBuffer writeLong(long value) {
        this.origin.writeLong(value);
        return this;
    }

    public long readLong() {
        return this.origin.readLong();
    }

    public PacketBuffer writeFloat(float value) {
        this.origin.writeFloat(value);
        return this;
    }

    public float readFloat() {
        return this.origin.readFloat();
    }

    public PacketBuffer writeDouble(double value) {
        this.origin.writeDouble(value);
        return this;
    }

    public double readDouble() {
        return this.origin.readDouble();
    }

    public short readShort() {
        return this.origin.readShort();
    }

    public PacketBuffer writeShort(short value) {
        this.origin.writeShort(value);
        return this;
    }

    public PacketBuffer writeUniqueId(UUID uniqueId) {
        this.origin.writeLong(uniqueId.getMostSignificantBits());
        this.origin.writeLong(uniqueId.getLeastSignificantBits());
        return this;
    }

    public UUID readUniqueId() {
        return new UUID(this.origin.readLong(), this.origin.readLong());
    }

    public PacketBuffer writeEnum(Enum<?> value) {
        this.origin.writeInt(value.ordinal());
        return this;
    }

    public <T extends Enum<?>> T readEnum(Class<T> clazz) {
        int ordinal = this.origin.readInt();
        T[] constants = clazz.getEnumConstants();
        if (ordinal < 0 || ordinal >= constants.length) {
            throw new IllegalArgumentException("Invalid enum ordinal: " + ordinal);
        }
        return constants[ordinal];
    }

    public <T> void writeList(List<T> list) {
        writeInt(list.size());
        if (list.isEmpty()) {
            return;
        }

        String type = list.get(0).getClass().getName();
        writeString(type);

        switch (type) {
            case "java.lang.String" -> list.forEach(item -> writeString((String) item));
            case "java.lang.Boolean" -> list.forEach(item -> writeBoolean((Boolean) item));
            case "java.lang.Integer" -> list.forEach(item -> writeInt((Integer) item));
            case "java.lang.Long" -> list.forEach(item -> writeLong((Long) item));
            case "java.lang.Float" -> list.forEach(item -> writeFloat((Float) item));
            case "java.lang.Double" -> list.forEach(item -> writeDouble((Double) item));
            case "java.lang.Short" -> list.forEach(item -> writeShort((Short) item));
            case "java.util.UUID" -> list.forEach(item -> writeUniqueId((UUID) item));
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    public <T> List<T> readList(Class<T> valueType) {
        int size = readInt();
        if (size == 0) {
            return new ArrayList<T>();
        }

        String type = readString();
        List<T> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            switch (type) {
                case "java.lang.String" -> list.add(valueType.cast(readString()));
                case "java.lang.Boolean" -> list.add(valueType.cast(readBoolean()));
                case "java.lang.Integer" -> list.add(valueType.cast(readInt()));
                case "java.lang.Long" -> list.add(valueType.cast(readLong()));
                case "java.lang.Float" -> list.add(valueType.cast(readFloat()));
                case "java.lang.Double" -> list.add(valueType.cast(readDouble()));
                case "java.lang.Short" -> list.add(valueType.cast(readShort()));
                case "java.util.UUID" -> list.add(valueType.cast(readUniqueId()));
                default -> throw new IllegalArgumentException("Unsupported type: " + type);
            }
        }

        return list;
    }

    public <K, V> void writeMap(HashMap<K, V> map) {
        writeInt(map.size());
        if (map.isEmpty()) {
            return;
        }

        String keyType = map.keySet().iterator().next().getClass().getName();
        String valueType = map.values().iterator().next().getClass().getName();

        writeString(keyType);
        writeString(valueType);

        map.forEach((key, value) -> {
            switch (keyType) {
                case "java.lang.String" -> writeString((String) key);
                case "java.lang.Boolean" -> writeBoolean((Boolean) key);
                case "java.lang.Integer" -> writeInt((Integer) key);
                case "java.lang.Long" -> writeLong((Long) key);
                case "java.lang.Float" -> writeFloat((Float) key);
                case "java.lang.Double" -> writeDouble((Double) key);
                case "java.lang.Short" -> writeShort((Short) key);
                case "java.util.UUID" -> writeUniqueId((UUID) key);
                default -> throw new IllegalArgumentException("Unsupported type: " + keyType);
            }

            switch (valueType) {
                case "java.lang.String" -> writeString((String) value);
                case "java.lang.Boolean" -> writeBoolean((Boolean) value);
                case "java.lang.Integer" -> writeInt((Integer) value);
                case "java.lang.Long" -> writeLong((Long) value);
                case "java.lang.Float" -> writeFloat((Float) value);
                case "java.lang.Double" -> writeDouble((Double) value);
                case "java.lang.Short" -> writeShort((Short) value);
                case "java.util.UUID" -> writeUniqueId((UUID) value);
                default -> throw new IllegalArgumentException("Unsupported type: " + valueType);
            }
        });
    }

    public <K, V> HashMap<K, V> readMap(Class<K> keyType, Class<V> valueType) {
        int size = readInt();
        if (size == 0) {
            return new HashMap<K, V>();
        }

        String keyClass = readString();
        String valueClass = readString();
        HashMap<K, V> map = new HashMap<>(size);

        for (int i = 0; i < size; i++) {
            K key;
            V value;

            switch (keyClass) {
                case "java.lang.String" -> key = keyType.cast(readString());
                case "java.lang.Boolean" -> key = keyType.cast(readBoolean());
                case "java.lang.Integer" -> key = keyType.cast(readInt());
                case "java.lang.Long" -> key = keyType.cast(readLong());
                case "java.lang.Float" -> key = keyType.cast(readFloat());
                case "java.lang.Double" -> key = keyType.cast(readDouble());
                case "java.lang.Short" -> key = keyType.cast(readShort());
                case "java.util.UUID" -> key = keyType.cast(readUniqueId());
                default -> throw new IllegalArgumentException("Unsupported type: " + keyClass);
            }

            switch (valueClass) {
                case "java.lang.String" -> value = valueType.cast(readString());
                case "java.lang.Boolean" -> value = valueType.cast(readBoolean());
                case "java.lang.Integer" -> value = valueType.cast(readInt());
                case "java.lang.Long" -> value = valueType.cast(readLong());
                case "java.lang.Float" -> value = valueType.cast(readFloat());
                case "java.lang.Double" -> value = valueType.cast(readDouble());
                case "java.lang.Short" -> value = valueType.cast(readShort());
                case "java.util.UUID" -> value = valueType.cast(readUniqueId());
                default -> throw new IllegalArgumentException("Unsupported type: " + valueClass);
            }

            map.put(key, value);
        }

        return map;
    }

}
