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

package allay.master;

import allay.api.AllayInstance;
import allay.master.network.NetworkManager;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public class AllayMaster extends AllayInstance {

    private NetworkManager networkManager;

    @Override
    public void onStartup() {
        networkManager = new NetworkManager(this, "cool-token");
        networkManager.bootSync();

        commandManager().register(getClass().getPackage().getName() + ".command", AllayMaster.class, this);
        commandManager().sort();
    }

    @Override
    public void onShutdown() {
        networkManager.shutdownSync();
    }

}
