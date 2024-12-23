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

package allay.master.service;

import allay.api.network.packet.Packet;
import allay.api.player.CloudPlayer;
import allay.api.service.CloudGroup;
import allay.api.service.CloudService;
import allay.api.service.CloudServiceState;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CloudServiceImpl implements CloudService {

    @Override
    public CloudGroup group() {
        return null;
    }

    @Override
    public CloudServiceState state() {
        return null;
    }

    @Override
    public UUID systemId() {
        return null;
    }

    @Override
    public int orderId() {
        return 0;
    }

    @Override
    public String hostname() {
        return "";
    }

    @Override
    public String ip() {
        return "";
    }

    @Override
    public int port() {
        return 0;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void execute(String command) {
    }

    @Override
    public CompletableFuture<Integer> onlinePlayerCount() {
        return null;
    }

    @Override
    public CompletableFuture<CloudPlayer> onlinePlayers() {
        return null;
    }

    @Override
    public void sendPacket(Packet packet) {
    }

    @Override
    public CompletableFuture<Void> reload() {
        return null;
    }

}
