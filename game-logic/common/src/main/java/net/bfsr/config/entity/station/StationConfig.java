package net.bfsr.config.entity.station;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.config.Configurable;
import net.bfsr.engine.config.entity.GameObjectConfig;

@Getter
@Setter
@Configurable
public final class StationConfig extends GameObjectConfig {
    private float destroyTimeInSeconds;
    private float shieldOutlineOffset;
    private float shieldBlurSize;
}