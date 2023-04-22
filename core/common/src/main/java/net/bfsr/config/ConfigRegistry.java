package net.bfsr.config;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConfigRegistry<CONFIG_TYPE, DATA_TYPE> {
    private final TMap<String, DATA_TYPE> registry = new THashMap<>();
    private final List<DATA_TYPE> list = new ArrayList<>();
    private final Path folder;
    private final Class<CONFIG_TYPE> configClass;
    private final Function<CONFIG_TYPE, String> nameFunction;
    private final BiFunction<CONFIG_TYPE, Integer, DATA_TYPE> mapFunction;

    public ConfigRegistry(Path folder, Class<CONFIG_TYPE> configClass, Function<CONFIG_TYPE, String> nameFunction, BiFunction<CONFIG_TYPE, Integer, DATA_TYPE> mapFunction) {
        this.folder = folder;
        this.configClass = configClass;
        this.nameFunction = nameFunction;
        this.mapFunction = mapFunction;
    }

    public void init() {
        ConfigLoader.loadFromFiles(folder, configClass, this::add);
    }

    private void add(CONFIG_TYPE config) {
        try {
            DATA_TYPE data = mapFunction.apply(config, list.size());
            list.add(data);
            registry.put(nameFunction.apply(config), data);
        } catch (Exception e) {
            throw new RuntimeException("Can't map config " + config + " to data", e);
        }
    }

    public DATA_TYPE get(String key) {
        return registry.get(key);
    }

    public DATA_TYPE get(int index) {
        return list.get(index);
    }
}