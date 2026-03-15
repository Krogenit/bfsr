package net.bfsr.config.entity.damageable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.config.Configurable;
import net.bfsr.engine.config.Vector2iConfigurable;
import net.bfsr.engine.config.entity.GameObjectConfig;

@Configurable
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DamageableRigidBodyConfig extends GameObjectConfig {
    private Vector2iConfigurable damageMaskSize;
    private float minDistanceBetweenVerticesSq;
    private float bufferDistance;
    private float bufferYOffset;
}
