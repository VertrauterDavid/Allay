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

package allay.api.service;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.File;

@Accessors(fluent = true)
@Getter
public enum ServiceVersion {

    // todo: add more pre-defined versions and the possibility to add own versions without recompiling the project

    Velocity("Velocity", "-", "shutdown", true, 40500),

    Paper_1_19_2("Paper 1.19.2", "-", "stop", false),
    Paper_1_19_4("Paper 1.19.4", "-", "stop", false),
    Paper_1_20_2("Paper 1.20.2", "-", "stop", false),
    Paper_1_20_4("Paper 1.20.4", "-", "stop", false),
    Paper_1_20_6("Paper 1.20.6", "-", "stop", false),
    Paper_1_21_4("Paper 1.21.4", "-", "stop", false);

    private final String displayName;
    private final String downloadUrl;
    private final String shutdownCommand;
    private final boolean proxy;
    private final int startPort;

    private final File jarFile;

    ServiceVersion(String displayName, String downloadUrl, String shutdownCommand, boolean proxy) {
        this(displayName, downloadUrl, shutdownCommand, proxy, 40700);
    }

    ServiceVersion(String displayName, String downloadUrl, String shutdownCommand, boolean proxy, int startPort) {
        this.displayName = displayName;
        this.downloadUrl = downloadUrl;
        this.shutdownCommand = shutdownCommand;
        this.proxy = proxy;
        this.startPort = startPort;

        this.jarFile = new File("storage/serviceVersions/" + displayName().toLowerCase() + ".jar");
    }

    public void download() {
        // todo
    }

    public boolean downloaded() {
        return jarFile.exists();
    }

}
