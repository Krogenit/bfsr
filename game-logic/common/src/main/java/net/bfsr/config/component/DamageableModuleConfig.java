package net.bfsr.config.component;

import lombok.Getter;
import net.bfsr.engine.config.Configurable;

@Getter
@Configurable
public class DamageableModuleConfig {
    private float hp;
}