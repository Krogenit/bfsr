package net.bfsr.config;

import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

@Log4j2
public final class ConfigLoader {
    private static final Moshi MOSHI = new Moshi.Builder().build();

    @Nullable
    public static <T> T load(File file, Class<T> type) {
        try {
            return MOSHI.adapter(type).indent("    ").fromJson(Files.readString(file.toPath()));
        } catch (IOException | JsonDataException e) {
            log.error("Error during loading json file {}", file, e);
        }

        return null;
    }

    public static void loadFromFiles(File folder, Consumer<File> fileConsumer) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    loadFromFiles(file, fileConsumer);
                } else if (file.getName().endsWith(".json")) {
                    fileConsumer.accept(file);
                }
            }
        }
    }

    public static <T> void save(File file, T object, Class<T> objectClass) {
        String json = MOSHI.adapter(objectClass).indent("    ").toJson(object);

        try {
            Files.writeString(file.toPath(), json);
        } catch (IOException e) {
            log.error("Error during saving json file {}", file, e);
        }
    }
}
