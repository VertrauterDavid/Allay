package allay.api.player;

import allay.api.interfaces.Sendable;
import allay.api.network.packet.PacketBuffer;
import allay.api.network.packet.packets.player.PlayerConnectPacket;
import allay.api.network.packet.packets.player.PlayerMessagePacket;
import allay.api.network.packet.packets.player.PlayerTitlePacket;
import allay.api.service.CloudService;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
@Getter
@Setter
public class CloudPlayer implements Sendable {

    private String name;
    private UUID uniqueId;

    private String server;
    private String proxy;

    public void sendMessage(String message) {
        currentProxy().thenAccept(service -> service.sendPacket(new PlayerMessagePacket(uniqueId, message, PlayerMessagePacket.Type.MESSAGE)));
    }

    public void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        currentProxy().thenAccept(service -> service.sendPacket(new PlayerTitlePacket(uniqueId, title, subTitle, fadeIn, stay, fadeOut)));
    }

    public void sendActionbar(String message) {
        currentProxy().thenAccept(service -> service.sendPacket(new PlayerMessagePacket(uniqueId, message, PlayerMessagePacket.Type.ACTIONBAR)));
    }

    public void connect(String serviceName) {
        currentProxy().thenAccept(service -> service.sendPacket(new PlayerConnectPacket(uniqueId, serviceName)));
    }

    @Override
    public void read(PacketBuffer buffer) {
        // todo
    }

    @Override
    public void write(PacketBuffer buffer) {
        // todo
    }

    public CompletableFuture<CloudService> currentServer() {
        // todo - return variable from service manager - CompletableFuture#completedFuture()
        return null;
    }

    public CompletableFuture<CloudService> currentProxy() {
        // todo - return variable from service manager - CompletableFuture#completedFuture()
        return null;
    }

    @Override
    public String toString() {
        return "CloudPlayer{" +
                "name='" + name + '\'' +
                ", uniqueId=" + uniqueId +
                ", server='" + server + '\'' +
                ", proxy='" + proxy + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloudPlayer that = (CloudPlayer) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uniqueId);
    }

}
