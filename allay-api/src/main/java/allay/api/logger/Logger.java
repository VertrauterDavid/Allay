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

package allay.api.logger;

import allay.api.AllayInstance;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("all")
public class Logger {

    public static final boolean DEBUG = false;
    public static final boolean PACKETS = false;

    private final AllayInstance allayInstance;
    private final SimpleDateFormat dateFormat;
    private final AtomicReference<String> loggerOutput = new AtomicReference<>("");

    public Logger(AllayInstance allayInstance) {
        this.allayInstance = allayInstance;
        this.dateFormat = new SimpleDateFormat("HH:mm:ss");
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));

        new ArrayList<org.apache.log4j.Logger>(Collections.list(LogManager.getCurrentLoggers())).forEach(logger -> logger.setLevel(Level.OFF));
        LogManager.getRootLogger().setLevel(Level.OFF);
    }

    public void info(String message) {
        log(LogType.INFO, message);
    }

    public void warning(String message) {
        log(LogType.WARNING, message);
    }

    public void error(String message) {
        log(LogType.ERROR, message);
    }

    public void debug(String message) {
        log(LogType.DEBUG, message);
    }

    public void log(LogType logType, String message) {
        if (message.contains("\n")) {
            for (String line : message.split("\n")) {
                log(logType, line);
            }
            return;
        }

        String line = "§r" + message + "§r";
        if (logType != null && logType != LogType.NONE) {
            line = "§r[" + dateFormat.format(new Date()) + " " + logType.color() + logType.display() + "§r]: " + message + "§r";
        }

        if (logType == LogType.DEBUG && !(DEBUG)) return;

        if (allayInstance.consoleManager() != null) {
            allayInstance.consoleManager().terminal().writer().println(AnsiColor.toColorCode(line));
            allayInstance.consoleManager().terminal().flush();
        } else {
            System.out.println(AnsiColor.toColorCode(line));
        }

        loggerOutput.accumulateAndGet(line + "\n", (prev, curr) -> prev + curr);
    }

    public void exception(Throwable throwable) {
        if (throwable != null) {
            for (String line : throwable.toString().split("\n")) {
                log(LogType.ERROR, line);
            }
            for (StackTraceElement element : throwable.getStackTrace()) {
                log(LogType.ERROR, element.toString());
            }
        }
    }

}
