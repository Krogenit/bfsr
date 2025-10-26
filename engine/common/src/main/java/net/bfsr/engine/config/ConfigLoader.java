package net.bfsr.engine.config;

import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.util.PathHelper;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@Log4j2
public final class ConfigLoader {
    private static final Moshi MOSHI = new Moshi.Builder().build();
    private static final String INDENT = "  ";
    private static final String FORMAT = ".json";

    public static <T> T load(Path file, Class<T> type) {
        try {
            return MOSHI.adapter(type).indent(INDENT).fromJson(Files.readString(file));
        } catch (IOException | JsonDataException e) {
            log.error("Error during loading json file {}", file, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> void loadFromFiles(Path folder, Class<T> configClass, LoadedFileConsumer<T> fileConsumer) {
        if (!folder.toFile().exists()) {
            return;
        }

        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fileName = file.getFileName().toString();
                    if (fileName.endsWith(FORMAT)) {
                        T loadedConfig = load(file, configClass);

                        String path = PathHelper.getLocalFileFolderPath(folder, file);
                        String fileNameWithoutExtension = PathHelper.getFileNameWithoutExtension(fileName);
                        if (loadedConfig instanceof Config config) {
                            config.setPath(path);
                            config.setName(fileNameWithoutExtension);
                        }

                        fileConsumer.accept(path, fileNameWithoutExtension, loadedConfig);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Can't load from folder " + folder, e);
        }
    }

    public static <T> void save(Path file, T object) {
        String json = MOSHI.adapter((Class<T>) object.getClass()).indent(INDENT).toJson(object);

        try {
            Files.writeString(file, json);
        } catch (IOException e) {
            log.error("Error during saving json file {}", file, e);
        }
    }
}