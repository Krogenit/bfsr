package net.bfsr.client.settings;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.config.ConfigLoader;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("TransientFieldInNonSerializableClass")
@Getter
@Setter
@Log4j2
public class ClientSettings {
    private final transient Option[] values = Option.values();
    private Map<Option, Object> options = new HashMap<>();

    public void saveSettings() {
        for (int i = 0; i < values.length; i++) {
            Option option = values[i];
            options.put(option, option.getValue());
        }

        ConfigLoader.save(getFile(), this, ClientSettings.class);
    }

    public void readSettings() {
        Path file = getFile();
        if (file.toFile().exists()) {
            ClientSettings clientSettings = ConfigLoader.load(file, ClientSettings.class);
            for (Map.Entry<Option, Object> entry : clientSettings.options.entrySet()) {
                entry.getKey().setValue(entry.getValue());
            }
        }
    }

    public Path getFile() {
        return Path.of(".", "client_settings.json");
    }
}