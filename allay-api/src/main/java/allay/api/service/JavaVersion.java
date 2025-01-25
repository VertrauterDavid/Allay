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
public enum JavaVersion {

    DEFAULT("default", null); // system java environment

    private final String displayName;
    private final String downloadUrl;
    private final String downloadPath;
    private final String extractedPath;

    JavaVersion(String displayName, String downloadUrl) {
        this.displayName = displayName;
        this.downloadUrl = downloadUrl;
        this.downloadPath = new File("storage/javaVersions/").getAbsolutePath();
        this.extractedPath = new File("storage/javaVersions/" + displayName + "/").getAbsolutePath();
    }

    public void download() {
        // todo
    }

    public boolean downloaded() {
        if (this == DEFAULT) return true;
        return new File("storage/java/" + displayName + "/").exists();
    }

    public String command() {
        if (this == DEFAULT) return "java";
        return new File(extractedPath + "/bin/java").getAbsolutePath();
    }

}
