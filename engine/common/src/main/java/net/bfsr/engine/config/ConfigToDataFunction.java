package net.bfsr.engine.config;

@FunctionalInterface
public interface ConfigToDataFunction<CONFIG_TYPE, DATA_TYPE> {
    DATA_TYPE convert(CONFIG_TYPE config, String fileName, int index, int registryId);
}