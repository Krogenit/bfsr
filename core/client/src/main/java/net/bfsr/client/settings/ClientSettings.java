package net.bfsr.client.settings;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.config.ConfigLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Log4j2
public class ClientSettings {
    private final Option[] values = Option.values();
    private Map<Option, Object> options = new HashMap<>();

    public void saveSettings() {
        for (int i = 0; i < values.length; i++) {
            Option option = values[i];
            options.put(option, option.getValue());
        }

        ConfigLoader.save(getFile(), this, ClientSettings.class);
    }

    public void readSettings() {
        File file = getFile();
        if (file.exists()) {
            ClientSettings clientSettings = ConfigLoader.load(file, ClientSettings.class);
            for (Map.Entry<Option, Object> entry : clientSettings.options.entrySet()) {
                entry.getKey().setValue(entry.getValue());
            }
        }
    }

    public File getFile() {
        return new File(".", "client_settings.json");
    }
}
