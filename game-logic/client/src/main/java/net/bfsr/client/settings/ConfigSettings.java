package net.bfsr.client.settings;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.settings.adapter.ConfigSettingsAdapter;
import net.bfsr.engine.config.ConfigLoader;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ConfigSettings implements Serializable {
    private final transient JsonAdapter<ConfigSettings> adapter = new Moshi.Builder()
            .add(new ConfigSettingsAdapter(new Moshi.Builder().build(), Object.class))
            .build().adapter(ConfigSettings.class);

    private Map<ClientSettings, Object> options = new HashMap<>();

    public void save() {
        ClientSettings[] values = ClientSettings.values();

        for (int i = 0; i < values.length; i++) {
            ClientSettings option = values[i];
            options.put(option, option.getValue());
        }

        ConfigLoader.save(getFile(), this);
    }

    public void load() {
        Path file = getFile();
        if (file.toFile().exists()) {
            ConfigSettings config;

            try {
                config = adapter.fromJson(Files.readString(file));
            } catch (IOException | JsonDataException e) {
                throw new IllegalStateException("Error during loading settings config file " + file, e);
            }

            for (Map.Entry<ClientSettings, Object> entry : config.options.entrySet()) {
                entry.getKey().setValue(entry.getValue());
            }
        }
    }

    private Path getFile() {
        return Path.of(".", "client_settings.json");
    }
}