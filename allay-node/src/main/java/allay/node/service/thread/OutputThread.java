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

package allay.node.service.thread;

import allay.node.service.RunningService;
import allay.node.util.ColorUtil;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class OutputThread extends Thread {

    private final RunningService runningService;

    @Override
    public void run() {
        runningService.output(new AtomicReference<>());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(runningService.process().getInputStream()))) {
            String line;
            Pattern pattern = Pattern.compile("\u001B\\[38;5;([0-9]+)m");
            while ((line = reader.readLine()) != null) {
                if (line.contains("connected player")) {
                    continue;
                }

                // todo test this shit
                // used the 2nd one and had some color problems... - if the 1st is good, remove the 2nd
                String formattedLine = "<span style=\"color: rgba(255, 255, 255, 0.75);\">" + line.replaceAll("<|>", "");
                // String formattedLine = "<span style=\"color: rgba(255, 255, 255, 0.75);\">" + line.replaceAll("[<>]", "");
                Matcher matcher = pattern.matcher(formattedLine);
                StringBuilder stringBuilder = new StringBuilder();
                while (matcher.find()) {
                    int colorCode = Integer.parseInt(matcher.group(1));
                    String hexColor = "<span style=\"color: #" + ColorUtil.translateIntToHex(colorCode) + ";\">";
                    matcher.appendReplacement(stringBuilder, hexColor);
                }
                matcher.appendTail(stringBuilder);
                formattedLine = stringBuilder.toString();

                formattedLine = formattedLine.replaceAll("\u001B\\[0m", "</span>");
                if (!formattedLine.endsWith("</span>")) {
                    formattedLine += "</span>";
                }

                runningService.output().accumulateAndGet(formattedLine + "\n", (prev, curr) -> prev + curr);
            }
        } catch (IOException ignored) { }
    }

}
