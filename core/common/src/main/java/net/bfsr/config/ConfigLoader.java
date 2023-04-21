package net.bfsr.config;

import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

@Log4j2
public final class ConfigLoader {
    private static final Moshi MOSHI = new Moshi.Builder().build();
    private static final String INDENT = "    ";

    public static <T> T load(Path file, Class<T> type) {
        try {
            return MOSHI.adapter(type).indent(INDENT).fromJson(Files.readString(file));
        } catch (IOException | JsonDataException e) {
            log.error("Error during loading json file {}", file, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> void loadFromFiles(Path folder, Class<T> configClass, Consumer<T> fileConsumer) {
        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.getFileName().toString().endsWith(".json")) {
                        fileConsumer.accept(load(file, configClass));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Can't load from folder " + folder, e);
        }
    }

    public static <T> void save(Path file, T object, Class<T> objectClass) {
        String json = MOSHI.adapter(objectClass).indent(INDENT).toJson(object);

        try {
            Files.writeString(file, json);
        } catch (IOException e) {
            log.error("Error during saving json file {}", file, e);
        }
    }
}