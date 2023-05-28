package net.bfsr.client.settings;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.config.ConfigLoader;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Log4j2
public class ConfigSettings {
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
            ConfigSettings config = ConfigLoader.load(file, ConfigSettings.class);
            for (Map.Entry<ClientSettings, Object> entry : config.options.entrySet()) {
                entry.getKey().setValue(entry.getValue());
            }
        }
    }

    private Path getFile() {
        return Path.of(".", "client_settings.json");
    }
}