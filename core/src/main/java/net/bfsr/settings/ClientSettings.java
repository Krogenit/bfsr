package net.bfsr.settings;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Log4j2
public class ClientSettings implements IConfig {
    private final transient JsonAdapter<ClientSettings> adapter = new Moshi.Builder().build().adapter(ClientSettings.class).indent("    ");

    private Map<EnumOption, Object> options = new HashMap<>();

    public void saveSettings() {
        EnumOption[] values = EnumOption.values();
        for (int i = 0; i < values.length; i++) {
            EnumOption option = values[i];
            options.put(option, option.getValue());
        }

        String json = adapter.toJson(this);
        try {
            Files.writeString(getFile().toPath(), json);
        } catch (IOException e) {
            log.error("Can't save settings", e);
        }
    }

    public void readSettings() {
        File file = getFile();
        if (file.exists()) {
            try {
                String string = Files.readString(file.toPath());
                ClientSettings settings = adapter.fromJson(string);
                for (Map.Entry<EnumOption, Object> entry : settings.options.entrySet()) {
                    entry.getKey().setValue(entry.getValue());
                }
            } catch (IOException | JsonDataException e) {
                log.error("Can't load settings", e);
            }
        }
    }

    @Override
    public File getFile() {
        return new File(".", "client_settings.json");
    }
}
