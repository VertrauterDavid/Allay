package allay.api.service;

import allay.api.interfaces.ChannelAppender;
import allay.api.interfaces.Sendable;
import allay.api.network.packet.Packet;
import allay.api.network.packet.PacketBuffer;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
@Getter
@Setter
public class CloudService implements Sendable, ChannelAppender {

    private CloudGroup group = new CloudGroup();
    private CloudServiceState state;

    private UUID systemId;
    private int orderId;

    private String node;
    private String ip;
    private int port;

    public String name() {
        return group.name() + "-" + orderId;
    }

    public String hostname() {
        return ip + ":" + port;
    }

    @Override
    public void read(PacketBuffer buffer) {
        group = new CloudGroup();
        group.read(buffer);

        state = buffer.readEnum(CloudServiceState.class);
        systemId = buffer.readUniqueId();
        orderId = buffer.readInt();
        node = buffer.readString();
        ip = buffer.readString();
        port = buffer.readInt();
    }

    @Override
    public void write(PacketBuffer buffer) {
        group.write(buffer);

        buffer.writeEnum(state);
        buffer.writeUniqueId(systemId);
        buffer.writeInt(orderId);
        buffer.writeString(node);
        buffer.writeString(ip);
        buffer.writeInt(port);
    }

    @Override
    public void sendPacket(Packet packet) {
        // will be overridden in the implementation
    }

    @Override
    public String toString() {
        return "CloudService{" +
                "group=" + group +
                ", state=" + state +
                ", systemId=" + systemId +
                ", orderId=" + orderId +
                ", node='" + node + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CloudService service = (CloudService) o;
        return Objects.equals(systemId, service.systemId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(systemId);
    }

}
