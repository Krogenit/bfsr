package net.bfsr.config;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Log4j2
public class ConfigLoader {
    private static final Gson GSON = new GsonBuilder()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getDeclaringClass().getAnnotation(Configurable.class) == null && f.getAnnotation(Configurable.class) == null || f.hasModifier(Modifier.TRANSIENT);
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .excludeFieldsWithModifiers(Modifier.TRANSIENT)
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    @Nullable
    public static <T> T load(File file, Class<T> type) {
        if (file.exists()) {
            try {
                JsonReader reader = new JsonReader(new FileReader(file, StandardCharsets.UTF_8));
                return GSON.fromJson(reader, type);
            } catch (JsonIOException | JsonSyntaxException | IOException e) {
                log.error("Error during load config file {}", file, e);
            }
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
}
