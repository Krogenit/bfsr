package net.bfsr.client.particle;

import net.bfsr.client.entity.bullet.Bullet;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.effect.ParticleEffect;
import net.bfsr.entity.GameObject;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class ParticleEffectsRegistry {
    public static final ParticleEffectsRegistry INSTANCE = new ParticleEffectsRegistry();

    private final ParticleSpawnFunction[] spawnFunctions = new ParticleSpawnFunction[ParticleEffect.values().length];

    public void init() {
        spawnFunctions[ParticleEffect.SMALL_BULLET_DAMAGE_TO_SHIP.ordinal()] = (initiator, affected, contactX, contactY, normalX, normalY) -> {
            Ship ship = (Ship) affected;
            Bullet bullet = (Bullet) initiator;
            ShieldCommon shield = ship.getShield();
            Vector4f color = bullet.getColor();
            Vector2f bulletScale = bullet.getScale();
            if (shield == null || shield.getShield() <= 0) {
                Hull hull = ship.getHull();
                float velocityX = ship.getVelocity().x;
                float velocityY = ship.getVelocity().y;
                Random rand = ship.getWorld().getRand();
                if (hull.getHull() / hull.getMaxHull() < 0.5f && rand.nextInt(2) == 0) {
                    ParticleSpawner.spawnShipOst(1, contactX, contactY, velocityX + normalX * (rand.nextFloat() * 0.5f + 0.5f),
                            velocityY + normalY * (rand.nextFloat() * 0.5f + 0.5f), 0.5f);
                }
                ParticleSpawner.spawnSmallGarbage(1 + rand.nextInt(3), contactX, contactY, velocityX + normalX, velocityY + normalY,
                        1.1f * (rand.nextFloat() + 0.5f), 3.0f, 0.5f);
            }

            Vector2f position = bullet.getPosition();
            ParticleSpawner.spawnLight(position.x, position.y, bulletScale.x * 3.0f, 3.0f * 6.0f, color.x, color.y, color.z, 0.4f, 0.5f * 60.0f, true, RenderLayer.DEFAULT_ADDITIVE);
            ParticleSpawner.spawnDirectedSpark(contactX, contactY, normalX, normalY, bulletScale.x * 1.5f, color.x, color.y, color.z, color.w);
        };
    }

    public void emit(int id, GameObject initiator, GameObject affected, float contactX, float contactY, float normalX, float normalY) {
        spawnFunctions[id].emit(initiator, affected, contactX, contactY, normalX, normalY);
    }

    public interface ParticleSpawnFunction {
        void emit(GameObject initiator, GameObject affected, float contactX, float contactY, float normalX, float normalY);
    }
}