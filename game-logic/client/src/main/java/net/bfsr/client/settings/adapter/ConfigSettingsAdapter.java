package net.bfsr.client.settings.adapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;
import net.bfsr.client.settings.ClientSettings;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ConfigSettingsAdapter extends JsonAdapter<Map<ClientSettings, Object>> {
    private final JsonAdapter<Object> valueAdapter;

    public ConfigSettingsAdapter(Moshi moshi, Type valueType) {
        this.valueAdapter = moshi.adapter(valueType);
    }

    @ToJson
    @Override
    public void toJson(JsonWriter writer, Map<ClientSettings, Object> map) throws IOException {
        writer.beginObject();
        for (Map.Entry<ClientSettings, Object> entry : map.entrySet()) {
            if (entry.getKey() == null) {
                throw new JsonDataException("Map key is null at " + writer.getPath());
            }
            writer.promoteValueToName();
            writer.value(entry.getKey().toString());
            valueAdapter.toJson(writer, entry.getValue());
        }
        writer.endObject();
    }

    @FromJson
    @Override
    public Map<ClientSettings, Object> fromJson(JsonReader reader) throws IOException {
        Map<ClientSettings, Object> result = new HashMap<>();
        reader.beginObject();
        while (reader.hasNext()) {
            reader.promoteNameToValue();
            ClientSettings name;

            try {
                name = ClientSettings.valueOf(reader.nextString());
            } catch (IllegalArgumentException e) {
                name = null;
            }

            Object value = valueAdapter.fromJson(reader);

            if (name == null) {
                continue;
            }

            Object replaced = result.put(name, value);
            if (replaced != null) {
                throw new JsonDataException("Map key '" + name + "' has multiple values at path " + reader.getPath() + ": " + replaced
                        + " and " + value);
            }
        }
        reader.endObject();
        return result;
    }
}
