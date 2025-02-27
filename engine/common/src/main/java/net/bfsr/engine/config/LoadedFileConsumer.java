package net.bfsr.engine.config;

@FunctionalInterface
public interface LoadedFileConsumer<T> {
    void accept(String path, String fileName, T loadedFile);
}