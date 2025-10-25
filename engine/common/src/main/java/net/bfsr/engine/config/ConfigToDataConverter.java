package net.bfsr.engine.config;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.util.PathHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ConfigToDataConverter<CONFIG_TYPE, DATA_TYPE extends ConfigData> {
    @Setter
    @Getter
    private int id;
    private final TMap<String, DATA_TYPE> registry = new THashMap<>();
    private final List<DATA_TYPE> dataTypeList = new ArrayList<>();
    @Getter
    private final Path folder;
    private final Class<CONFIG_TYPE> configClass;
    private final BiFunction<String, CONFIG_TYPE, String> nameFunction;
    private final ConfigToDataFunction<CONFIG_TYPE, DATA_TYPE> mapFunction;

    public ConfigToDataConverter(String folder, Class<CONFIG_TYPE> configClass, BiFunction<String, CONFIG_TYPE, String> nameFunction,
                                 ConfigToDataFunction<CONFIG_TYPE, DATA_TYPE> mapFunction) {
        this(PathHelper.CONFIG.resolve(folder), configClass, nameFunction, mapFunction);
    }

    public ConfigToDataConverter(Path folder, Class<CONFIG_TYPE> configClass, BiFunction<String, CONFIG_TYPE, String> nameFunction,
                                 ConfigToDataFunction<CONFIG_TYPE, DATA_TYPE> mapFunction) {
        this.folder = folder;
        this.configClass = configClass;
        this.nameFunction = nameFunction;
        this.mapFunction = mapFunction;
    }

    public void init(int id) {
        this.id = id;
        ConfigLoader.loadFromFiles(folder, configClass, this::add);
    }

    public void add(String path, String fileName, CONFIG_TYPE config) {
        try {
            DATA_TYPE data = mapFunction.convert(config, fileName, dataTypeList.size(), id);
            add(data, nameFunction.apply(fileName, config));
        } catch (Exception e) {
            throw new RuntimeException("Can't map config " + config + " to data", e);
        }
    }

    protected void add(DATA_TYPE data, String name) {
        if (registry.containsKey(name)) {
            throw new IllegalStateException("Key name " + name + " already taken in config registry " + this.getClass().getSimpleName());
        }
        
        dataTypeList.add(data);
        registry.put(name, data);
    }

    public DATA_TYPE get(String key) {
        return registry.get(key);
    }

    public DATA_TYPE get(int index) {
        return dataTypeList.get(index);
    }

    public void remove(String key) {
        DATA_TYPE data = registry.remove(key);
        dataTypeList.remove(data);
    }

    public List<DATA_TYPE> getAll() {
        return dataTypeList;
    }
}