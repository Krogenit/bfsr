package net.bfsr.config;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import net.bfsr.engine.util.PathHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConfigToDataConverter<CONFIG_TYPE, DATA_TYPE> {
    private final TMap<String, DATA_TYPE> registry = new THashMap<>();
    private final List<DATA_TYPE> list = new ArrayList<>();
    @Getter
    private final Path folder;
    private final Class<CONFIG_TYPE> configClass;
    private final Function<CONFIG_TYPE, String> nameFunction;
    private final BiFunction<CONFIG_TYPE, Integer, DATA_TYPE> mapFunction;

    public ConfigToDataConverter(String folder, Class<CONFIG_TYPE> configClass, Function<CONFIG_TYPE, String> nameFunction, BiFunction<CONFIG_TYPE, Integer, DATA_TYPE> mapFunction) {
        this.folder = PathHelper.CONFIG.resolve(folder);
        this.configClass = configClass;
        this.nameFunction = nameFunction;
        this.mapFunction = mapFunction;
    }

    public void init() {
        ConfigLoader.loadFromFiles(folder, configClass, this::add);
    }

    public void add(CONFIG_TYPE config) {
        try {
            DATA_TYPE data = mapFunction.apply(config, list.size());
            add(data, nameFunction.apply(config));
        } catch (Exception e) {
            throw new RuntimeException("Can't map config " + config + " to data", e);
        }
    }

    protected void add(DATA_TYPE data, String name) {
        if (registry.containsKey(name))
            throw new IllegalStateException("Key name " + name + " already taken in config registry " + this.getClass().getSimpleName());
        list.add(data);
        registry.put(name, data);
    }

    public DATA_TYPE get(String key) {
        return registry.get(key);
    }

    public DATA_TYPE get(int index) {
        return list.get(index);
    }

    public void remove(String key) {
        DATA_TYPE data = registry.remove(key);
        list.remove(data);
    }

    public List<DATA_TYPE> getAll() {
        return list;
    }
}