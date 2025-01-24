package net.bfsr.config;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConfigConverterManager {
    private final List<ConfigToDataConverter<?, ?>> configRegistryList = new ArrayList<>();
    private final TIntObjectMap<ConfigToDataConverter<?, ?>> configConverterById = new TIntObjectHashMap<>();

    public ConfigConverterManager(ConfigToDataConverter<?, ?>... configToDataConverters) {
        Reflections reflections = new Reflections("net.bfsr.config");
        Set<Class<?>> classes = reflections.get(
                Scanners.SubTypes.of(Scanners.TypesAnnotated.with(ConfigConverter.class)).asClass());
        classes.forEach(aClass -> {
            try {
                registerConfigRegistry((ConfigToDataConverter<?, ?>) aClass.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("Failed to create config registry instance for " + aClass.getName(), e);
            }
        });

        for (int i = 0; i < configToDataConverters.length; i++) {
            registerConfigRegistry(configToDataConverters[i]);
        }
    }

    public void registerConfigRegistry(ConfigToDataConverter<?, ?> converter) {
        int id = configRegistryList.size();
        converter.init(id);
        configRegistryList.add(converter);
        configConverterById.put(id, converter);
    }

    public ConfigToDataConverter<?, ?> getConverter(int id) {
        return configConverterById.get(id);
    }

    @Nullable
    public <T> T getConverter(Class<T> configToDataConverterClass) {
        for (ConfigToDataConverter<?, ?> configToDataConverter : configConverterById.valueCollection()) {
            if (configToDataConverter.getClass() == configToDataConverterClass) {
                return (T) configToDataConverter;
            }
        }

        return null;
    }
}