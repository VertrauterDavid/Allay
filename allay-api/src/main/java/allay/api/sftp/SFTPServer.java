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

import lombok.RequiredArgsConstructor;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import java.io.File;

@RequiredArgsConstructor
public class SFTPServer {

    // todo
    // only test code, not production code

    private final int port;
    private final String username;
    private final String password;
    private final String baseDir;

    private SshServer sshServer;

    public void boot() {
        try {
            sshServer = SshServer.setUpDefaultServer();
            sshServer.setPort(port);
            sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("storage/config/hostkey.ser").toPath()));

            sshServer.setPasswordAuthenticator((user, pass, session) -> user.equals(username) && pass.equals(password));
            sshServer.setFileSystemFactory(new VirtualFileSystemFactory(new File(baseDir).toPath()));

            sshServer.start();
            System.out.println("SFTP server started on port " + port);
        } catch (Exception e) {
            System.out.println("Failed to start SFTP server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            if (sshServer != null && sshServer.isStarted()) {
                sshServer.stop();
                System.out.println("SFTP server stopped.");
            }
        } catch (Exception e) {
            System.out.println("Failed to stop SFTP server: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
