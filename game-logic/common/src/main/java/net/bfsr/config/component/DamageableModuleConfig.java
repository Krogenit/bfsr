package net.bfsr.config.component;

import lombok.Getter;
import net.bfsr.config.Configurable;

@Getter
@Configurable
public class DamageableModuleConfig {
    private float hp;
}