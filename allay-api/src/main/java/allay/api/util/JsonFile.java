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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("all")
@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public class JsonFile {

    private final File file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final JsonParser jsonParser = new JsonParser();

    public void set(String key, Object value) {
        try {
            if (!(file.exists())) {
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }

            Map<String, Object> dataMap = getObject();
            dataMap.put(key, value);

            FileWriter writer = new FileWriter(file);
            JsonElement jsonElement = gson.toJsonTree(dataMap);
            String prettyJsonString = gson.toJson(jsonElement);
            writer.write(prettyJsonString);
            writer.flush();
            writer.close();
        } catch (IOException ignored) { }
    }

    private void setDefault(String key, Object value) {
        if (get(key) != null) return;
        set(key, value);
    }

    private Object get(String key) {
        Map<String, Object> dataMap = getObject();
        return dataMap.getOrDefault(key, null);
    }

    private Map<String, Object> getObject() {
        try {
            if (file.exists()) {
                FileReader reader = new FileReader(file);
                Map<String, Object> dataMap = gson.fromJson(reader, LinkedHashMap.class);
                reader.close();
                return dataMap != null ? dataMap : new LinkedHashMap<>();
            }
        } catch (IOException ignored) { }
        return new LinkedHashMap<>();
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public JsonFile setString(String key, String value) {
        set(key, value);
        return this;
    }

    public JsonFile setStringDefault(String key, String value) {
        setDefault(key, value);
        return this;
    }

    public boolean getBoolean(String key) {
        return (boolean) get(key);
    }

    public JsonFile setBoolean(String key, boolean value) {
        set(key, value);
        return this;
    }

    public JsonFile setBooleanDefault(String key, boolean value) {
        setDefault(key, value);
        return this;
    }

    public long getLong(String key) {
        return ((Number) get(key)).longValue();
    }

    public JsonFile setLong(String key, long value) {
        set(key, value);
        return this;
    }

    public JsonFile setLongDefault(String key, long value) {
        setDefault(key, value);
        return this;
    }

    public double getDouble(String key) {
        return (double) get(key);
    }

    public JsonFile setDouble(String key, double value) {
        set(key, value);
        return this;
    }

    public JsonFile setDoubleDefault(String key, double value) {
        setDefault(key, value);
        return this;
    }

    public <T> List<T> getList(String key, Class<T> type) {
        List<?> rawList = (List<?>) get(key);
        if (rawList == null) return null;

        try {
            List<T> typedList = new ArrayList<>();
            for (Object item : rawList) {
                T valueCast;

                // Konvertiere Werte korrekt, wenn der gewünschte Typ Long ist
                if (type == Long.class && item instanceof Number) {
                    valueCast = type.cast(((Number) item).longValue());
                } else {
                    valueCast = gson.fromJson(gson.toJson(item), type);
                }

                typedList.add(valueCast);
            }
            return typedList;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("List contains elements of a different type", e);
        }
    }

    public <T> JsonFile setList(String key, List<T> list) {
        set(key, list);
        return this;
    }

    public <T> JsonFile setListDefault(String key, List<T> list) {
        setDefault(key, list);
        return this;
    }

    public <K, V> HashMap<K, V> getMap(String key, Class<K> keyType, Class<V> valueType) {
        Map<?, ?> rawMap = (Map<?, ?>) get(key);
        if (rawMap == null) return null;

        try {
            HashMap<K, V> typedMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                K keyCast = keyType.cast(entry.getKey());

                V valueCast;
                if (valueType == Long.class && entry.getValue() instanceof Number) {
                    valueCast = valueType.cast(((Number) entry.getValue()).longValue());
                } else {
                    valueCast = gson.fromJson(gson.toJson(entry.getValue()), valueType);
                }

                typedMap.put(keyCast, valueCast);
            }
            return typedMap;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Map contains keys or values of a different type", e);
        }
    }

    public <K, V> JsonFile setMap(String key, HashMap<K, V> map) {
        set(key, map);
        return this;
    }

    public <K, V> JsonFile setMapDefault(String key, HashMap<K, V> map) {
        setDefault(key, map);
        return this;
    }

}
