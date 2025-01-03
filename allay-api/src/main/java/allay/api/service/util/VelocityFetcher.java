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

package allay.api.service.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

@UtilityClass
public class VelocityFetcher {

    private static final String VERSIONS = "https://api.papermc.io/v2/projects/velocity/";
    private static final String BUILDS = "https://api.papermc.io/v2/projects/velocity/versions/%version/";
    private static final String DOWNLOAD = "https://api.papermc.io/v2/projects/velocity/versions/%version/builds/%build/downloads/velocity-%version-%build.jar";

    public static String getDownloadUrl() {
        JSONObject versionsJson = new JSONObject(sendRequest(VERSIONS));
        JSONArray versionsArray = versionsJson.getJSONArray("versions");
        String version = versionsArray.getString(versionsArray.length() - 1);

        JSONObject buildsJson = new JSONObject(sendRequest(BUILDS.replace("%version", version)));
        JSONArray buildsArray = buildsJson.getJSONArray("builds");
        int build = buildsArray.getInt(buildsArray.length() - 1);

        return DOWNLOAD
                .replace("%version", version)
                .replace("%build", String.valueOf(build));
    }

    @SneakyThrows
    private static String sendRequest(String urlString) {
        HttpURLConnection connection = (HttpURLConnection) new URI(urlString).toURL().openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        return response.toString();
    }

}
