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

package allay.api.util;

import allay.api.logger.Logger;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class FileUtil {

    @SneakyThrows
    public static void copyFile(File file, File newFile) {
        if (!(file.exists())) return;

        File parentDir = newFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void copyFiles(File folder, String directory) {
        if (!folder.exists() || folder.listFiles() == null) return;

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            File newFile = new File(directory, file.getName());

            if (file.isDirectory()) {
                copyFiles(file, newFile.getAbsolutePath());
            } else {
                copyFile(file, newFile);
            }
        }
    }

    public static void createDirectory(File file) {
        try {
            Files.createDirectories(Paths.get(file.getAbsolutePath()));
        } catch (IOException ignored) { }
    }

    public static void delete(Logger logger, File file) {
        String name = file.getPath();
        logger.debug("Deleting '" + name + "'...");

        try {
            new ProcessBuilder("/bin/sh", "-c", "rm -r " + file.getAbsolutePath()).start().waitFor();
            logger.debug("Successfully deleted '" + name + "'!");
        } catch (IOException | InterruptedException exception) {
            logger.debug("Could not delete '" + name + "'!");
        }
    }

    public void wget(Logger logger, File file, String url) throws IOException, InterruptedException {
        if (SystemUtil.isWindows()) return;

        ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", "cd " + file.getParentFile().getAbsolutePath() + " && wget --quiet --tries=3 --timeout=30 -O " + file.getName() + " " + url);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroy();
            throw new IOException("wget timed out");
        }
    }

}
