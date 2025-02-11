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

package allay.node;

import allay.api.AllayInstance;
import allay.api.network.channel.NetworkChannelState;
import allay.api.network.packet.packets.sys.NodeStatusPacket;
import allay.node.network.NetworkManager;
import allay.node.service.ServiceManager;
import allay.node.util.DownloadUtil;
import allay.node.web.WebManager;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Accessors(fluent = true)
@Getter
public class AllayNode extends AllayInstance {

    private NetworkManager networkManager;
    private ServiceManager serviceManager;
    private WebManager webManager;

    @Override
    public void onStartup() {
        // download all required files
        DownloadUtil.downloadAll(logger());

        networkManager = new NetworkManager(this);
        networkManager.bootSync();
        logger().info("   §7> successfully connected to master on port §c" + networkManager.port());

        webManager = new WebManager(this);
        webManager.boot().join();
        logger().info("   §7> successfully booted web server on port §c" + webManager.port());

        serviceManager = new ServiceManager(this);
        serviceManager.load();
        logger().info("   §7> successfully loaded service manager");
        logger().info("");

        commandManager().register(getClass().getPackage().getName() + ".command", AllayNode.class, this);
        commandManager().sort();

        // send a packet to the master - we are now ready to handle services
        networkManager.channel().send(new NodeStatusPacket(NetworkChannelState.READY));
    }

    @Override
    public void onShutdown() {
        networkManager.channel().sendIfActive(new NodeStatusPacket(NetworkChannelState.CLOSED));

        if (serviceManager != null) {
            serviceManager.shutdown();
        }

        // todo - implement networkManager.shutdownSync() - atm it's throwing useless TimeoutExceptions
        try {
            networkManager.shutdown().get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) { }
    }

}
