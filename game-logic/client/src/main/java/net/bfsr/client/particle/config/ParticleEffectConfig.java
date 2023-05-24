package net.bfsr.client.particle.config;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.Configurable;
import net.bfsr.config.ConfigurableSound;
import net.bfsr.config.NameableConfig;
import net.bfsr.engine.renderer.particle.RenderLayer;

import java.util.List;

@Setter
@Getter
@Configurable
public class ParticleEffectConfig extends NameableConfig {
    private List<String> texturePaths;
    private float spawnOverTime;
    private int minSpawnCount, maxSpawnCount;
    private float minPosX, minPosY, maxPosX, maxPosY;
    private float minVelocityX, minVelocityY, maxVelocityX, maxVelocityY;
    private float minAngle, maxAngle;
    private float minAngularVelocity, maxAngularVelocity;
    private float minSizeX, minSizeY, maxSizeX, maxSizeY;
    private float minSizeVelocity, maxSizeVelocity;
    private float r, g, b, a;
    private float minAlphaVelocity, maxAlphaVelocity;
    private boolean isAlphaFromZero;
    private RenderLayer renderLayer;
    private List<ConfigurableSound> soundEffects;
    private float sourceSizeXMultiplier, sourceSizeYMultiplier;
    private float sourceVelocityXMultiplier, sourceVelocityYMultiplier;
    private String editorPath;
    private int treeIndex;

    public void processDeprecated() {}

    public String getPath() {
        return editorPath != null ? editorPath.isEmpty() ? name : editorPath + "/" + name : "";
    }
}