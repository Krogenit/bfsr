package net.bfsr.config.entity.damageable;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.config.entity.GameObjectConfigData;
import org.joml.Vector2i;

@Getter
@Log4j2
public class DamageableRigidBodyConfigData extends GameObjectConfigData {
    public static final float MIN_DISTANCE_BETWEEN_VERTICES_SQ = 0.3f;
    private static final float MIN_BUFFER_DISTANCE = 0.1f;

    private final Vector2i damageMaskSize;
    private final float minDistanceBetweenVerticesSq;
    private final float bufferDistance;
    private final float bufferYOffset;

    public DamageableRigidBodyConfigData(DamageableRigidBodyConfig config, String fileName, int id, int registryId) {
        super(config, fileName, id, registryId);
        this.damageMaskSize = convert(config.getDamageMaskSize());
        this.minDistanceBetweenVerticesSq = config.getMinDistanceBetweenVerticesSq();
        if (minDistanceBetweenVerticesSq < MIN_DISTANCE_BETWEEN_VERTICES_SQ) {
            throw new IllegalArgumentException("Min distance between vertices should be greater than " + MIN_DISTANCE_BETWEEN_VERTICES_SQ +
                    " in config " + fileName);
        }

        this.bufferDistance = config.getBufferDistance();
        if (bufferDistance < MIN_BUFFER_DISTANCE) {
            throw new IllegalArgumentException("Min buffer distance should be greater than " + MIN_BUFFER_DISTANCE +
                    " in config " + fileName);
        }

        this.bufferYOffset = config.getBufferYOffset();
    }
}
