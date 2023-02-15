package net.bfsr.server;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.ConfigLoader;

import java.io.File;

@Getter
@Setter
public class ServerSettings {
    private String hostName = "127.0.0.1";
    private int port = 25565;

    public void saveSettings() {
        ConfigLoader.save(getFile(), this, ServerSettings.class);
    }

    public void readSettings() {
        ServerSettings serverSettings = ConfigLoader.load(getFile(), ServerSettings.class);
        if (serverSettings != null) {
            this.hostName = serverSettings.hostName;
            this.port = serverSettings.port;
        } else {
            saveSettings();
        }
    }

    public File getFile() {
        return new File(".", "server_settings.json");
    }
}
