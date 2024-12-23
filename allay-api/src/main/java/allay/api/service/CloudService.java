package allay.api.service;

import allay.api.interfaces.ChannelAppender;
import allay.api.interfaces.Named;
import allay.api.interfaces.Reloadable;
import allay.api.player.CloudPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CloudService extends Named, ChannelAppender {

    CloudGroup group();
    CloudServiceState state();

    UUID systemId();
    int orderId();

    String hostname();
    String ip();
    int port();

    void shutdown();
    void execute(String command);

    CompletableFuture<Integer> onlinePlayerCount();
    CompletableFuture<CloudPlayer> onlinePlayers();

    @Override
    default String name() {
        return group().name() + "-" + orderId();
    }

}
