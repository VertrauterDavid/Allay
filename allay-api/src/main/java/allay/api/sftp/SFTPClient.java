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

package allay.api.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;

@RequiredArgsConstructor
public class SFTPClient {

    // todo
    // only test code, not production code

    private static final String remoteDir = "/"; // Base remote directory
    private static final String localDir = "test/templates"; // Local directory

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public void fetchTemplates() {
        Session session = null;
        ChannelSftp channel = null;

        try {
            // Connect to SFTP server
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            System.out.println("Connected to SFTP server.");

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            // Delete existing local files
            File localDirectory = new File(localDir);
            if (!localDirectory.exists()) {
                localDirectory.mkdirs();
            } else {
                for (File file : localDirectory.listFiles()) {
                    file.delete();
                }
            }

            // List and download files
            Vector<ChannelSftp.LsEntry> files = channel.ls(remoteDir);
            int totalFiles = files.size();
            int completedFiles = 0;

            for (ChannelSftp.LsEntry entry : files) {
                if (!entry.getAttrs().isDir()) {
                    String remoteFilePath = remoteDir + entry.getFilename();
                    File localFile = new File(localDirectory, entry.getFilename());

                    try (FileOutputStream fos = new FileOutputStream(localFile)) {
                        channel.get(remoteFilePath, fos);
                    }

                    completedFiles++;
                    int progress = (int) ((completedFiles / (float) totalFiles) * 100);
                    System.out.println("Downloaded: " + entry.getFilename() + " (" + progress + "%)");
                }
            }
            System.out.println("All files downloaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

}
