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

package allay.node.web;

import allay.node.AllayNode;
import spark.Request;
import spark.Response;

public abstract class Route {

    protected AllayNode allayNode;
    protected String path;

    public Route(AllayNode allayNode, String path) {
        this.allayNode = allayNode;
        this.path = path;
        setup(path);
    }

    public abstract void setup(String path);
    public abstract void onRequest(Request request, Response response);

}
