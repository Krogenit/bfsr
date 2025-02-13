package net.bfsr.config.entity.ship;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.ColorConfigurable;
import net.bfsr.config.Configurable;
import net.bfsr.config.GameObjectConfig;
import net.bfsr.config.Vector2fConfigurable;
import net.bfsr.config.Vector2iConfigurable;
import net.bfsr.config.component.ModulesPolygonsConfig;

@Getter
@Setter
@Configurable
public final class ShipConfig extends GameObjectConfig {
    private float destroyTimeInSeconds;
    private ColorConfigurable effectsColor;
    private Vector2fConfigurable[] weaponSlotPositions;
    private ModulesPolygonsConfig modules;
    private Vector2iConfigurable damageMaskSize;
}