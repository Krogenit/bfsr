package net.bfsr.server;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.ConfigLoader;

import java.nio.file.Path;

@Getter
@Setter
public class ServerSettings {
    private String hostName = "127.0.0.1";
    private int port = 25565;

    public void saveSettings() {
        ConfigLoader.save(getPath(), this, ServerSettings.class);
    }

    public void readSettings() {
        Path path = getPath();
        if (!path.toFile().exists()) {
            saveSettings();
            return;
        }

        ServerSettings serverSettings = ConfigLoader.load(path, ServerSettings.class);
        this.hostName = serverSettings.hostName;
        this.port = serverSettings.port;
    }

    public Path getPath() {
        return Path.of(".", "server_settings.json");
    }
}