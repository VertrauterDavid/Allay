package allay.api.interfaces;

import java.util.concurrent.CompletableFuture;

public interface Reloadable {

    CompletableFuture<Void> reload();

}
