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

package allay.node.util;

import allay.api.logger.LogType;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    public static String translateMinecraftColor(String input) {
        Map<Character, String> colorMap = getCharacterStringMap();

        Map<Character, String> styleMap = new HashMap<>();
        styleMap.put('l', "font-weight: bold;");
        styleMap.put('m', "text-decoration: line-through;");
        styleMap.put('n', "text-decoration: underline;");

        Pattern pattern = Pattern.compile("§([0-9a-fA-Fr])|§([lmn])");
        Matcher matcher = pattern.matcher(input);
        StringBuilder stringBuilder = new StringBuilder();
        boolean resetStyle = false;

        while (matcher.find()) {
            char code = matcher.group(1) != null ? matcher.group(1).charAt(0) : matcher.group(2).charAt(0);
            String replacement;
            if (code == 'r') {
                replacement = "</span><span style=\"color: rgba(255, 255, 255, 0.75);\">";
                resetStyle = true;
            } else if (colorMap.containsKey(code)) {
                String color = colorMap.getOrDefault(code, "rgba(255, 255, 255, 0.75);");
                replacement = (resetStyle ? "</span>" : "") + "<span style=\"color: " + color + ";\">";
                resetStyle = false;
            } else {
                String style = styleMap.getOrDefault(code, "");
                replacement = (resetStyle ? "</span>" : "") + "<span style=\"" + style + "\">";
                resetStyle = false;
            }
            matcher.appendReplacement(stringBuilder, replacement);
        }
        matcher.appendTail(stringBuilder);

        return stringBuilder + "</span>";
    }

    private static Map<Character, String> getCharacterStringMap() {
        Map<Character, String> colorMap = new HashMap<>();
        colorMap.put('0', "#000000"); // Black
        colorMap.put('1', "#0000AA"); // Dark Blue
        colorMap.put('2', "#00AA00"); // Dark Green
        colorMap.put('3', "#00AAAA"); // Dark Aqua
        colorMap.put('4', "#AA0000"); // Dark Red
        colorMap.put('5', "#AA00AA"); // Dark Purple
        colorMap.put('6', "#FFAA00"); // Gold
        colorMap.put('7', "#AAAAAA"); // Gray
        colorMap.put('8', "#555555"); // Dark Gray
        colorMap.put('9', "#5555FF"); // Blue
        colorMap.put('a', "#55FF55"); // Green
        colorMap.put('b', "#55FFFF"); // Aqua
        colorMap.put('c', "#FF5555"); // Red
        colorMap.put('d', "#FF55FF"); // Light Purple
        colorMap.put('e', "#FFFF55"); // Yellow
        colorMap.put('f', "rgba(255, 255, 255, 0.75);");
        return colorMap;
    }

    public static String translateIntToHex(int color) {
        return switch (color) {
            case 0 -> "000000"; // black
            case 1 -> "800000"; // maroon
            case 2 -> "008000"; // green
            case 3 -> "808000"; // olive
            case 4 -> "000080"; // navy
            case 5 -> "800080"; // purple
            case 6 -> "008080"; // teal
            case 7 -> "c0c0c0"; // silver
            case 8 -> "808080"; // grey
            case 9 -> "ff0000"; // red
            case 10 -> "00ff00"; // lime
            case 11 -> "ffff00"; // yellow
            case 12 -> "0000ff"; // blue
            case 13 -> "ff00ff"; // fuchsia
            case 14 -> "00ffff"; // aqua
            case 15 -> "ffffff"; // white
            default -> "ffffff"; // default to white
        };
    }

    public static String translateLogTypes(String input) {
        for (LogType logType : LogType.values()) {
            if (logType.color() == null) continue;

            String logTypeString = logType.name() + "]: ";
            if (input.contains(logTypeString)) {
                input = input.replace(logTypeString, logType.color() + logType.name() + "§7]: ");
            }
        }

        return input;
    }

}
