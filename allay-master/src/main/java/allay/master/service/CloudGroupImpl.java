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

import allay.api.service.CloudGroup;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CloudGroupImpl implements CloudGroup {

    @Override
    public String name() {
        return "";
    }

    @Override
    public long memory() {
        return 0;
    }

    @Override
    public long minInstances() {
        return 0;
    }

    @Override
    public long maxInstances() {
        return 0;
    }

    @Override
    public boolean staticGroup() {
        return false;
    }

    @Override
    public String version() {
        return "";
    }

    @Override
    public String javaVersion() {
        return "";
    }

    @Override
    public List<Node> nodes() {
        return List.of();
    }

    @Override
    public List<Template> templates() {
        return List.of();
    }

    @Override
    public CompletableFuture<Void> reload() {
        return null;
    }

}
