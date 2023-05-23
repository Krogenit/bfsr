package net.bfsr.config;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConfigConverterManager {
    public static final ConfigConverterManager INSTANCE = new ConfigConverterManager();

    private final List<ConfigToDataConverter<?, ?>> configRegistryList = new ArrayList<>();

    public void init() {
        Reflections reflections = new Reflections("net.bfsr.config");
        Set<Class<?>> classes = reflections.get(Scanners.SubTypes.of(Scanners.TypesAnnotated.with(ConfigConverter.class)).asClass());
        classes.forEach(aClass -> registerConfigRegistry(getInstance(aClass)));

        for (int i = 0; i < configRegistryList.size(); i++) {
            configRegistryList.get(i).init();
        }
    }

    private ConfigToDataConverter<?, ?> getInstance(Class<?> aClass) {
        try {
            return ((ConfigToDataConverter<?, ?>) aClass.getDeclaredField("INSTANCE").get(null));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException("Can't get instance of " + aClass.getName(), e);
        }
    }

    public void registerConfigRegistry(ConfigToDataConverter<?, ?> configRegistry) {
        configRegistryList.add(configRegistry);
    }
}