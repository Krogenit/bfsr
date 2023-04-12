package net.bfsr.client.particle;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.entity.bullet.Bullet;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.util.PathHelper;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.config.ConfigLoader;
import net.bfsr.effect.ParticleEffect;
import net.bfsr.entity.GameObject;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Random;

@Log4j2
public class ParticleEffectsRegistry {
    public static final ParticleEffectsRegistry INSTANCE = new ParticleEffectsRegistry();

    @Getter
    private final File effectsFolder = new File(PathHelper.CONFIG, "particleeffect");
    private final ParticleSpawnFunction[] spawnFunctions = new ParticleSpawnFunction[ParticleEffect.values().length];
    private final TMap<String, net.bfsr.client.particle.ParticleEffect> registry = new THashMap<>();

    public void load() {
        ConfigLoader.loadFromFiles(effectsFolder, net.bfsr.client.particle.ParticleEffect.class, particleEffect -> {
            particleEffect.processDeprecated();
            add(particleEffect);
        });
    }

    public void add(net.bfsr.client.particle.ParticleEffect particleEffect) {
        if (particleEffect.getEditorPath().isEmpty()) {
            registry.put(particleEffect.getName(), particleEffect);
        } else {
            registry.put(particleEffect.getEditorPath() + "/" + particleEffect.getName(), particleEffect);
        }
    }

    public void remove(String name) {
        registry.remove(name);
    }

    public void delete(String name) {
        remove(name);

        File file = new File(effectsFolder, name + ".json");

        if (file.exists()) {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                log.error("Failed to delete particle effect {}", name, e);
            }
        }
    }

    public Collection<net.bfsr.client.particle.ParticleEffect> getAllEffects() {
        return registry.values();
    }

    public void init() {
        load();

        registry.forEachValue(object -> {
            object.init();
            return true;
        });

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

    public net.bfsr.client.particle.ParticleEffect getEffect(String name) {
        return registry.get(name);
    }
}