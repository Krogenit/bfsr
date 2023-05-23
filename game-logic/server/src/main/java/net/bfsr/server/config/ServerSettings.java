package net.bfsr.server.config;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.ConfigLoader;

import java.nio.file.Path;

@Getter
@Setter
public class ServerSettings {
    private static final String SETTINGS_FILE_NAME = "server_settings.json";

    private String hostName = "127.0.0.1";
    private int port = 34000;

    private String dataBaseServiceHost = "localhost";
    private int databaseServicePort = 7000;

    public void saveSettings(Path path) {
        ConfigLoader.save(path, this, ServerSettings.class);
    }

    public static ServerSettings load() {
        Path path = Path.of(SETTINGS_FILE_NAME);
        if (path.toFile().exists()) {
            return ConfigLoader.load(path, ServerSettings.class);
        } else {
            ServerSettings settings = new ServerSettings();
            settings.saveSettings(path);
            return settings;
        }
    }
}