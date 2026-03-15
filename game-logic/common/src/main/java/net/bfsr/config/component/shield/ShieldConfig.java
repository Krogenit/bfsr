package net.bfsr.config.component.shield;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.bfsr.engine.config.Configurable;

@Configurable
@Accessors(fluent = true)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShieldConfig {
    private float maxShield;
    private float regenInSeconds;
    private float rebuildTimeInSeconds;
}