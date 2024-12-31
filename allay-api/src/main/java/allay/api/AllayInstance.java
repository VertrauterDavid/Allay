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

package allay.api;

import allay.api.console.ConsoleManager;
import allay.api.console.command.CommandManager;
import allay.api.logger.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public abstract class AllayInstance {

    private final Logger logger;
    private final ConsoleManager consoleManager;
    private final CommandManager commandManager;

    private boolean shuttingDown;

    @Setter
    private boolean skipShutdownHook = false;

    public AllayInstance() {
        logger = new Logger(this);
        logger.info("");
        logger.info("§9         _   _ _            §7___ _             _ ");
        logger.info("§9        /_\\ | | |__ _ _  _ §7/ __| |___ _  _ __| |");
        logger.info("§9       / _ \\| | / _` | || |§7 (__| / _ \\ || / _` |");
        logger.info("§9      /_/ \\_\\_|_\\__,_|\\_, |§7\\___|_\\___/\\_,_\\__,_|");
        logger.info("§9                      |__/");
        logger.info("");
        logger.info("   §7> §9allay-cloud §7provided by §9github.com/VertrauterDavid");
        logger.info("   §7> the simplest all in one cloud system");
        logger.info("");

        consoleManager = new ConsoleManager(this);
        commandManager = new CommandManager();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        logger.debug("");
        logger.debug("Debug mode is enabled!");
        logger.debug("");

        onStartup();
        consoleManager.init();
    }

    private void shutdown() {
        shuttingDown = true;
        if (skipShutdownHook) return;

        logger.info("");
        logger.info("Shutting down...");
        logger.info("");

        sleep(500);
        onShutdown();

        consoleManager.stop();
    }

    public abstract void onStartup();
    public abstract void onShutdown();

    @SneakyThrows
    public void sleep(long millis) {
        Thread.sleep(millis);
    }

}
