package net.bfsr.client.config.particle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bfsr.engine.config.Config;
import net.bfsr.engine.config.Configurable;
import net.bfsr.engine.config.ConfigurableSound;
import net.bfsr.engine.renderer.particle.RenderLayer;

import java.util.List;

@Getter
@Configurable
@AllArgsConstructor
public class ParticleEffectConfig extends Config {
    private final List<String> texturePaths;
    private final float spawnOverTime;
    private final int minSpawnCount, maxSpawnCount;
    private final float minPosX, minPosY, maxPosX, maxPosY;
    private final float minVelocityX, minVelocityY, maxVelocityX, maxVelocityY;
    private final float minAngle, maxAngle;
    private final float minAngularVelocity, maxAngularVelocity;
    private final float minSizeX, minSizeY, maxSizeX, maxSizeY;
    private final float minSizeVelocity, maxSizeVelocity;
    private final float r, g, b, a;
    private final float minAlphaVelocity, maxAlphaVelocity;
    private final boolean isAlphaFromZero;
    private final RenderLayer renderLayer;
    private final List<ConfigurableSound> soundEffects;
    private final float sourceSizeXMultiplier, sourceSizeYMultiplier;
    private final float sourceVelocityXMultiplier, sourceVelocityYMultiplier;

    void processDeprecated() {}
}