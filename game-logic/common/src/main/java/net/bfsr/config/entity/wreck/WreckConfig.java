package net.bfsr.config.entity.wreck;

import lombok.Getter;
import net.bfsr.engine.config.Configurable;
import net.bfsr.engine.config.entity.GameObjectConfig;
import net.bfsr.entity.wreck.WreckType;

@Getter
@Configurable
final class WreckConfig extends GameObjectConfig {
    private WreckType type;
    private String fireTexture;
    private String sparkleTexture;
}