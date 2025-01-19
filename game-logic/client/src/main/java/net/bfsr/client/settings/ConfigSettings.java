package net.bfsr.client.settings;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.settings.adapter.ConfigSettingsAdapter;
import net.bfsr.config.ConfigLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Log4j2
public class ConfigSettings {
    private transient final JsonAdapter<ConfigSettings> adapter = new Moshi.Builder()
            .add(new ConfigSettingsAdapter(new Moshi.Builder().build(), Object.class))
            .build().adapter(ConfigSettings.class);

    private Map<ClientSettings, Object> options = new HashMap<>();

    public void save() {
        ClientSettings[] values = ClientSettings.values();

        for (int i = 0; i < values.length; i++) {
            ClientSettings option = values[i];
            options.put(option, option.getValue());
        }

        ConfigLoader.save(getFile(), this, ConfigSettings.class);
    }

    public void load() {
        Path file = getFile();
        if (file.toFile().exists()) {
            ConfigSettings config;

            try {
                config = adapter.fromJson(Files.readString(file));
            } catch (IOException | JsonDataException e) {
                log.error("Error during loading settings config file {}", file, e);
                throw new RuntimeException(e);
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