package allay.api.service;

import allay.api.interfaces.Sendable;
import allay.api.network.packet.PacketBuffer;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
@Getter
@Setter
public class CloudGroup implements Sendable {

    private String name;
    private String displayName;
    private long memory;
    private long minInstances;
    private long maxInstances;
    private boolean staticGroup;

    private ServiceVersion version;
    private JavaVersion javaVersion;

    private HashMap<String, Long> nodes;
    private List<String> templates;

    @Override
    public void read(PacketBuffer buffer) {
        name = buffer.readString();
        displayName = buffer.readString();
        memory = buffer.readLong();
        minInstances = buffer.readLong();
        maxInstances = buffer.readLong();
        staticGroup = buffer.readBoolean();
        version = buffer.readEnum(ServiceVersion.class);
        javaVersion = buffer.readEnum(JavaVersion.class);
        nodes = buffer.readMap(String.class, Long.class);
        templates = buffer.readList(String.class);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(name);
        buffer.writeString(displayName);
        buffer.writeLong(memory);
        buffer.writeLong(minInstances);
        buffer.writeLong(maxInstances);
        buffer.writeBoolean(staticGroup);
        buffer.writeEnum(version);
        buffer.writeEnum(javaVersion);
        buffer.writeMap(nodes);
        buffer.writeList(templates);
    }

    @Override
    public String toString() {
        return "CloudGroup{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", memory=" + memory +
                ", minInstances=" + minInstances +
                ", maxInstances=" + maxInstances +
                ", staticGroup=" + staticGroup +
                ", version='" + version.displayName() + '\'' +
                ", javaVersion='" + javaVersion.displayName() + '\'' +
                ", nodes=" + nodes +
                ", templates=" + templates +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloudGroup group = (CloudGroup) o;
        return Objects.equals(name, group.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

}
