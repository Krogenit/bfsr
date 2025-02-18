package net.bfsr.config.entity.damageable;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.Configurable;
import net.bfsr.config.Vector2iConfigurable;
import net.bfsr.config.entity.GameObjectConfig;

@Getter
@Setter
@Configurable
public class DamageableRigidBodyConfig extends GameObjectConfig {
    private Vector2iConfigurable damageMaskSize;
    private float minDistanceBetweenVerticesSq;
    private float bufferDistance;
    private float bufferYOffset;
}
