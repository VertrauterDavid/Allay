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

package allay.api.console.command;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

@SuppressWarnings("rawtypes")
@Accessors(fluent = true)
@Getter
public class CommandManager {

    private final ArrayList<Command> commands = new ArrayList<>();

    public void register(Command command) {
        commands.add(command);
    }

    public void register(String path, Class<?> parameterType, Object parameter) {
        for (Class<?> clazz : new Reflections(path).getSubTypesOf(Command.class)) {
            try {
                Command command = (Command) clazz.getConstructor(parameterType).newInstance(parameter);
                commands.add(command);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) { }
        }
    }

    public void sort() {
        commands.sort(Comparator.comparingInt(Command::weight));
    }

    public void executeCommand(String line) {
        String[] parts = line.split(" ");
        String name = parts[0];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        commands.stream()
                .filter(command -> command.name().equalsIgnoreCase(name) || command.hasAlias(name))
                .forEach(command -> command.execute(args));
    }

}
