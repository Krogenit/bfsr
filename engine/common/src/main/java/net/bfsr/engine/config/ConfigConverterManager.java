package net.bfsr.engine.config;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.bfsr.engine.util.PathHelper;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigConverterManager {
    private final List<ConfigToDataConverter<?, ?>> configRegistryList = new ArrayList<>();
    private final TIntObjectMap<ConfigToDataConverter<?, ?>> configConverterById = new TIntObjectHashMap<>();
    private final Map<ConfigToDataConverter<?, ?>, Set<Path>> appliedOverrides = new HashMap<>();

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

    private void registerConfigRegistry(ConfigToDataConverter<?, ?> converter) {
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

    public void applyProfileOverrides(@Nullable Path overlayRoot) {
        resetProfileOverrides();

        if (overlayRoot == null || !overlayRoot.toFile().exists()) {
            return;
        }

        for (int i = 0; i < configRegistryList.size(); i++) {
            ConfigToDataConverter<?, ?> converter = configRegistryList.get(i);
            if (!converter.getFolder().startsWith(PathHelper.CONFIG)) {
                continue;
            }

            Set<Path> overridden = converter.applyProfileOverrides(overlayRoot);
            if (!overridden.isEmpty()) {
                appliedOverrides.put(converter, overridden);
            }
        }
    }

    private void resetProfileOverrides() {
        for (Map.Entry<ConfigToDataConverter<?, ?>, Set<Path>> entry : appliedOverrides.entrySet()) {
            entry.getKey().resetOverrides(entry.getValue());
        }

        appliedOverrides.clear();
    }
}
