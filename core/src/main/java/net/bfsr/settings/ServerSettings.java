package net.bfsr.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.IConfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Getter
@Setter
public class ServerSettings implements IConfig {

    private transient final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private String hostName = "127.0.0.1";
    private int port = 25565;

    public void saveSettings() {
        String json = gson.toJson(this);

        try {
            FileWriter fileWriter = new FileWriter(getFile());
            fileWriter.write(json);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readSettings() {
        File file = getFile();
        if (file.exists()) {
            try {
                JsonReader reader = new JsonReader(new FileReader(file));
                IConfig config = gson.fromJson(reader, this.getClass());
                for (Field field : this.getClass().getDeclaredFields()) {
                    if (!Modifier.isTransient(field.getModifiers())) {
                        Field field1 = config.getClass().getDeclaredField(field.getName());
                        field.setAccessible(true);
                        field1.setAccessible(true);
                        field.set(this, field1.get(config));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            saveSettings();
        }
    }

    @Override
    public File getFile() {
        return new File(".", "server_settings.json");
    }
}
