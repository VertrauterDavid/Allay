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

import allay.api.logger.Logger;
import allay.api.service.util.VelocityFetcher;
import allay.api.util.FileUtil;
import allay.api.util.SystemUtil;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.File;
import java.io.IOException;

@Accessors(fluent = true)
@Getter
public enum ServiceVersion {

    /*

    Proxies:            40500
    Geyser:             40600
    Servers:            40700

    Paper Versions:     https://api.papermc.io/v2/projects/paper/
    Paper Builds:       https://api.papermc.io/v2/projects/paper/versions/1.19.2/

    Purpur Versions:    https://api.purpurmc.org/v2/purpur/

     */

    BUNGEECORD_LATEST("Bungeecord", "https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar", "stop", true, false, 40500),
    VELOCITY_LATEST("Velocity", "-", "shutdown", true, false, 40500),
    GEYSER_LATEST("Geyser", "https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/standalone", "stop", false, true, 40600),

    /*
    SPIGOT_1_19_2("Spigot 1.19.2", "https://download.getbukkit.org/spigot/spigot-1.19.2.jar"),
    SPIGOT_1_19_4("Spigot 1.19.4", "https://download.getbukkit.org/spigot/spigot-1.19.4.jar"),
    SPIGOT_1_20_2("Spigot 1.20.2", "https://download.getbukkit.org/spigot/spigot-1.20.2.jar"),
    SPIGOT_1_20_4("Spigot 1.20.4", "https://download.getbukkit.org/spigot/spigot-1.20.4.jar"),
    SPIGOT_1_20_6("Spigot 1.20.6", "https://download.getbukkit.org/spigot/spigot-1.20.6.jar"),
    SPIGOT_1_21("Spigot 1.21", "https://download.getbukkit.org/spigot/spigot-1.21.jar"),
     */

    PAPER_1_19_2("Paper 1.19.2", "https://api.papermc.io/v2/projects/paper/versions/1.19.2/builds/307/downloads/paper-1.19.2-307.jar"),
    PAPER_1_19_4("Paper 1.19.4", "https://api.papermc.io/v2/projects/paper/versions/1.19.4/builds/550/downloads/paper-1.19.4-550.jar"),
    PAPER_1_20("Paper 1.20", "https://api.papermc.io/v2/projects/paper/versions/1.20/builds/17/downloads/paper-1.20-17.jar"),
    PAPER_1_20_2("Paper 1.20.2", "https://api.papermc.io/v2/projects/paper/versions/1.20.2/builds/318/downloads/paper-1.20.2-318.jar"),
    PAPER_1_20_4("Paper 1.20.4", "https://api.papermc.io/v2/projects/paper/versions/1.20.4/builds/499/downloads/paper-1.20.4-499.jar"),
    PAPER_1_20_6("Paper 1.20.6", "https://api.papermc.io/v2/projects/paper/versions/1.20.6/builds/151/downloads/paper-1.20.6-151.jar"),
    PAPER_1_21("Paper 1.21", "https://api.papermc.io/v2/projects/paper/versions/1.21/builds/130/downloads/paper-1.21-130.jar"),
    PAPER_1_21_4("Paper 1.21.4", "https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/72/downloads/paper-1.21.4-72.jar"),

    PURPUR_1_19_2("Purpur 1.19.2", "https://api.purpurmc.org/v2/purpur/1.19.2/latest/download"),
    PURPUR_1_19_4("Purpur 1.19.4", "https://api.purpurmc.org/v2/purpur/1.19.4/latest/download"),
    PURPUR_1_20("Purpur 1.20", "https://api.purpurmc.org/v2/purpur/1.20/latest/download"),
    PURPUR_1_20_2("Purpur 1.20.2", "https://api.purpurmc.org/v2/purpur/1.20.2/latest/download"),
    PURPUR_1_20_4("Purpur 1.20.4", "https://api.purpurmc.org/v2/purpur/1.20.4/latest/download"),
    PURPUR_1_20_6("Purpur 1.20.6", "https://api.purpurmc.org/v2/purpur/1.20.6/latest/download"),
    PURPUR_1_21("Purpur 1.21", "https://api.purpurmc.org/v2/purpur/1.21/latest/download"),
    PURPUR_1_21_4("Purpur 1.21.4", "https://api.purpurmc.org/v2/purpur/1.21.4/latest/download");

    private final String displayName;
    private final String downloadUrl;
    private final String shutdownCommand;
    private final boolean proxy;
    private final boolean bedrock;
    private final int startPort;

    private final File jarFile;

    ServiceVersion(String displayName, String downloadUrl) {
        this(displayName, downloadUrl, "stop", false, false, 40700);
    }

    ServiceVersion(String displayName, String downloadUrl, String shutdownCommand, boolean proxy, boolean bedrock, int startPort) {
        this.displayName = displayName;
        this.downloadUrl = downloadUrl;
        this.shutdownCommand = shutdownCommand;
        this.proxy = proxy;
        this.bedrock = bedrock;
        this.startPort = startPort;

        this.jarFile = new File("storage/serviceVersions/" + displayName().toLowerCase().replaceAll(" ", "_") + ".jar");
    }

    public void download(Logger logger) {
        if (downloaded() || SystemUtil.isWindows()) return;
        if (!(jarFile.getParentFile().exists() || jarFile.getParentFile().mkdirs())) return;

        logger.info("Downloading §a" + displayName + " §7from " + downloadUrl.substring(8).split("/")[0] + "...");

        try {
            String downloadUrl = (this == VELOCITY_LATEST) ? VelocityFetcher.getDownloadUrl() : this.downloadUrl;
            FileUtil.wget(logger, jarFile, downloadUrl);

            logger.info("Download of §a" + displayName + " §7completed!");
        } catch (IOException | InterruptedException exception) {
            logger.error("Download of §c" + displayName + " §7failed!");
            logger.exception(exception);
        }
    }

    public boolean downloaded() {
        return jarFile.exists();
    }

}
