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

package test;

import allay.api.util.JsonFile;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonTest {

    private final JsonFile file = new JsonFile(new File("test.json"));

    @Test
    public void testJsonShit() {


        // write
        file.setString("name", "dödel");

        List<String> list1 = new ArrayList<>();
        list1.add("node1");
        list1.add("node2");
        file.setList("nodes1", list1);

        List<Long> list2 = new ArrayList<>();
        list2.add(1L);
        list2.add(5L);
        file.setList("list2", list2);

        List<Double> list3= new ArrayList<>();
        list3.add(1.5D);
        list3.add(7D);
        file.setList("list3", list3);

        HashMap<String, Integer> map1 = new HashMap<>();
        map1.put("node1", 1);
        map1.put("node2", 2);
        file.setMap("nodes2", map1);


        // read
        System.out.println(file.getString("name"));
        file.getList("nodes1", String.class).forEach(System.out::println);
        file.getList("list2", Long.class).forEach(System.out::println);
        file.getList("list3", Double.class).forEach(System.out::println);
        file.getMap("nodes2", String.class, Integer.class).forEach((key, value) -> System.out.println(key + " -> " + value));


    }

}
