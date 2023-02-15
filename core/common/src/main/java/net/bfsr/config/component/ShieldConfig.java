package net.bfsr.config.component;

import lombok.Getter;
import net.bfsr.config.Configurable;

@Configurable
@Getter
public class ShieldConfig {
    private String name;
    private String texture;
    private float maxShield;
    private float shieldRegen;
    private float rebuildTime;
}
