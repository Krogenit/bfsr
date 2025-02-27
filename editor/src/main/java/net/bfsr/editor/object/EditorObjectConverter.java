package net.bfsr.editor.object;

import net.bfsr.engine.config.Config;

public interface EditorObjectConverter<CONFIG_TYPE extends Config, PROPERTIES_TYPE extends ObjectProperties> {
    CONFIG_TYPE from(PROPERTIES_TYPE properties);
    PROPERTIES_TYPE to(CONFIG_TYPE config);
}