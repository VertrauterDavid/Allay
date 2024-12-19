package allay.api.player;

import allay.api.interfaces.Named;
import allay.api.interfaces.Reloadable;
import allay.api.service.CloudService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CloudPlayer extends Named, Reloadable {

    UUID uniqueId();

    CompletableFuture<CloudService> currentServer();
    CompletableFuture<CloudService> currentProxy();

    void sendMessage(String message);
    void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut);
    void sendActionbar(String message);
    void connect(String service);

    default void sendTitle(String title, String subtitle) {
        // default minecraft values
        sendTitle(title, subtitle, 10, 70, 20);
    }

    default void connect(CloudService service) {
        connect(service.name());
    }

}
