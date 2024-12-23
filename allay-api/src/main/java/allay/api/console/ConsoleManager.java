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

package allay.api.console;

import allay.api.AllayInstance;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Accessors(fluent = true)
@Getter
public class ConsoleManager {

    private final AllayInstance allayInstance;
    private Terminal terminal;
    private LineReader lineReader;
    private boolean exit = false;

    public ConsoleManager(AllayInstance allayInstance) {
        this.allayInstance = allayInstance;

        try {
            this.terminal = TerminalBuilder.builder().system(true).dumb(true).streams(System.in, System.out).encoding(StandardCharsets.UTF_8).build();
            this.lineReader = LineReaderBuilder.builder().terminal(terminal).option(LineReader.Option.DISABLE_EVENT_EXPANSION, true).option(LineReader.Option.AUTO_REMOVE_SLASH, false).option(LineReader.Option.INSERT_TAB, false).completer(new ConsoleCompleter()).build();
        } catch (IOException ignored) { }
    }

    public void init() {
        while (!(allayInstance.shuttingDown())) {
            try {
                String line = lineReader.readLine();
                if (line == null) break;
                allayInstance.commandManager().executeCommand(line);
            } catch (UserInterruptException exception) {
                if (!(exit)) {
                    exit = true;
                    System.exit(0);
                }
            }
        }
    }

    public void clear() {
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.flush();
        redraw();
    }

    public void redraw() {
        if (lineReader.isReading()) {
            lineReader.callWidget(LineReader.REDRAW_LINE);
            lineReader.callWidget(LineReader.REDISPLAY);
        }
    }

    public void stop() {
        try {
            if (lineReader.getTerminal() != null) {
                lineReader.getTerminal().close();
            }
        } catch (IOException ignored) { }
    }

}
