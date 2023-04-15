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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

@Log4j2
public class ParticleEffectsRegistry {
    public static final ParticleEffectsRegistry INSTANCE = new ParticleEffectsRegistry();

    @Getter
    private final Path effectsFolder = PathHelper.CONFIG.resolve("particleeffect");
    private final ParticleSpawnFunction[] spawnFunctions = new ParticleSpawnFunction[ParticleEffect.values().length];
    private final TMap<String, net.bfsr.client.particle.ParticleEffect> registry = new THashMap<>();

    public List<net.bfsr.client.particle.ParticleEffect> load() {
        List<net.bfsr.client.particle.ParticleEffect> allEffects = new ArrayList<>();
        ConfigLoader.loadFromFiles(effectsFolder, net.bfsr.client.particle.ParticleEffect.class, particleEffect -> {
            particleEffect.processDeprecated();
            allEffects.add(particleEffect);
        });

        findChild(allEffects);
        return allEffects;
    }

    private void findChild(List<net.bfsr.client.particle.ParticleEffect> allEffects) {
        for (int i = 0; i < allEffects.size(); i++) {
            net.bfsr.client.particle.ParticleEffect particleEffect = allEffects.get(i);
            for (int i1 = i + 1; i1 < allEffects.size(); i1++) {
                net.bfsr.client.particle.ParticleEffect particleEffect1 = allEffects.get(i1);
                if (particleEffect1.getPath().contains(particleEffect.getPath())) {
                    particleEffect.addChild(particleEffect1);
                } else if (particleEffect.getPath().contains(particleEffect1.getPath())) {
                    particleEffect1.addChild(particleEffect);
                }
            }
        }
    }

    public void add(net.bfsr.client.particle.ParticleEffect particleEffect) {
        registry.put(particleEffect.getPath(), particleEffect);
    }

    public void remove(String path) {
        registry.remove(path);
    }

    public Collection<net.bfsr.client.particle.ParticleEffect> getAllEffects() {
        return registry.values();
    }

    public void init() {
        List<net.bfsr.client.particle.ParticleEffect> particleEffects = load();

        for (int i = 0; i < particleEffects.size(); i++) {
            net.bfsr.client.particle.ParticleEffect particleEffect = particleEffects.get(i);
            particleEffect.init();
            add(particleEffect);
        }

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

    public net.bfsr.client.particle.ParticleEffect getEffectByPath(String path) {
        return registry.get(path);
    }
}