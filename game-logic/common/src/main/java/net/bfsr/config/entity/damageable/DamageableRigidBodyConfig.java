package net.bfsr.config.entity.damageable;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.config.Configurable;
import net.bfsr.engine.config.Vector2iConfigurable;
import net.bfsr.engine.config.entity.GameObjectConfig;

@Getter
@Setter
@Configurable
public class DamageableRigidBodyConfig extends GameObjectConfig {
    private Vector2iConfigurable damageMaskSize;
    private float minDistanceBetweenVerticesSq;
    private float bufferDistance;
    private float bufferYOffset;
}
