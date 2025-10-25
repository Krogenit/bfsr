package net.bfsr.config.entity.ship;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.ModulesPolygonsConfig;
import net.bfsr.config.entity.damageable.DamageableRigidBodyConfig;
import net.bfsr.engine.config.ColorConfigurable;
import net.bfsr.engine.config.Configurable;
import net.bfsr.engine.config.Vector2fConfigurable;

@Getter
@Setter
@Configurable
public final class ShipConfig extends DamageableRigidBodyConfig {
    private float destroyTimeInSeconds;
    private ColorConfigurable effectsColor;
    private Vector2fConfigurable[] weaponSlotPositions;
    private ModulesPolygonsConfig modules;
    private float shieldOutlineOffset;
    private float shieldBlurSize;
}