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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@Accessors(fluent = true)
@Getter
@Setter
public class CloudGroupImpl implements CloudGroup {

    private final ArrayList<CloudServiceImpl> services = new ArrayList<>();

    private final String name;
    private long memory;
    private long minInstances;
    private long maxInstances;
    private boolean staticGroup;

    private String version;
    private String javaVersion;

    private HashMap<String, Long> nodes;
    private List<String> templates;

    @Override
    public CompletableFuture<Void> reload() {
        // reload method is on master side absolutely useless
        return null;
    }

}
