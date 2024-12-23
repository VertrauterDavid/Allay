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

package allay.master.player;

import allay.api.player.CloudPlayer;
import allay.api.service.CloudService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CloudPlayerImpl implements CloudPlayer {

    @Override
    public String name() {
        return "";
    }

    @Override
    public UUID uniqueId() {
        return null;
    }

    @Override
    public CompletableFuture<CloudService> currentServer() {
        return null;
    }

    @Override
    public CompletableFuture<CloudService> currentProxy() {
        return null;
    }

    @Override
    public void sendMessage(String message) {
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    }

    @Override
    public void sendActionbar(String message) {
    }

    @Override
    public void connect(String service) {
    }

    @Override
    public CompletableFuture<Void> reload() {
        return null;
    }

}
