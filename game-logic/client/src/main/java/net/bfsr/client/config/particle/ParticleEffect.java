package net.bfsr.client.config.particle;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import lombok.Getter;
import net.bfsr.client.Client;
import net.bfsr.client.particle.Particle;
import net.bfsr.client.particle.ParticleManager;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.renderer.particle.ParticleRender;
import net.bfsr.client.sound.SoundEffect;
import net.bfsr.config.ConfigData;
import net.bfsr.config.ConfigurableSound;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.particle.RenderLayer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.util.PathHelper;
import net.bfsr.engine.util.RandomHelper;
import org.jbox2d.common.Rotation;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.bfsr.client.particle.ParticleManager.PARTICLE_POOL;

@Getter
public class ParticleEffect extends ConfigData {
    private AbstractTexture[] textures;
    private float spawnOverTime;
    private int minSpawnCount, maxSpawnCount;
    private float minPosX, minPosY, maxPosX, maxPosY;
    private float minVelocityX, minVelocityY, maxVelocityX, maxVelocityY;
    private float minAngle, maxAngle;
    private float minAngularVelocity, maxAngularVelocity;
    private float minSizeX, minSizeY, maxSizeX, maxSizeY;
    private float minSizeVelocity, maxSizeVelocity;
    private final Vector4f color = new Vector4f();
    private float minAlphaVelocity, maxAlphaVelocity;
    private boolean isAlphaFromZero;
    private RenderLayer renderLayer;
    private SoundEffect[] soundEffects;
    private float sourceSizeXMultiplier, sourceSizeYMultiplier;
    private float sourceVelocityXMultiplier, sourceVelocityYMultiplier;
    private String path;
    private int treeIndex;

    private double spawnTime;

    private final List<Particle> aliveParticles = new ArrayList<>();

    private final List<ParticleEffectSpawnRunnable> spawnRunnables = new ArrayList<>();
    private final List<ParticleEffect> childEffectsInstances = new ArrayList<>();
    private final XoRoShiRo128PlusRandom rand = new XoRoShiRo128PlusRandom();

    @FunctionalInterface
    private interface ParticleEffectSpawnRunnable {
        void spawn(float worldX, float worldY, float localX, float localY, float sizeX, float sizeY, float sin, float cos,
                   float velocityX, float velocityY, float r, float g, float b, float a, Consumer<Particle> updateLogic,
                   Consumer<ParticleRender> lastValuesUpdateConsumer);
    }

    @FunctionalInterface
    private interface ParticleParamFunction {
        float apply(float value);
    }

    public ParticleEffect(ParticleEffectConfig config, String fileName, int dataIndex, int registryId) {
        super(fileName, dataIndex, registryId);
        applyConfig(config);
    }

    public void applyConfig(ParticleEffectConfig config) {
        List<String> texturePaths = config.getTexturePaths();
        textures = new AbstractTexture[texturePaths.size()];
        for (int i = 0; i < texturePaths.size(); i++) {
            textures[i] = Engine.assetsManager.getTexture(PathHelper.convertPath(texturePaths.get(i)), GL.GL_CLAMP_TO_EDGE,
                    GL.GL_LINEAR);
        }

        this.spawnOverTime = config.getSpawnOverTime();
        this.minSpawnCount = config.getMinSpawnCount();
        this.maxSpawnCount = config.getMaxSpawnCount();
        this.minPosX = config.getMinPosX();
        this.minPosY = config.getMinPosY();
        this.maxPosX = config.getMaxPosX();
        this.maxPosY = config.getMaxPosY();
        this.minVelocityX = config.getMinVelocityX();
        this.minVelocityY = config.getMinVelocityY();
        this.maxVelocityX = config.getMaxVelocityX();
        this.maxVelocityY = config.getMaxVelocityY();
        this.minAngle = config.getMinAngle();
        this.maxAngle = config.getMaxAngle();
        this.minAngularVelocity = config.getMinAngularVelocity();
        this.maxAngularVelocity = config.getMaxAngularVelocity();
        this.minSizeX = config.getMinSizeX();
        this.minSizeY = config.getMinSizeY();
        this.maxSizeX = config.getMaxSizeX();
        this.maxSizeY = config.getMaxSizeY();
        this.minSizeVelocity = config.getMinSizeVelocity();
        this.maxSizeVelocity = config.getMaxSizeVelocity();
        this.color.set(config.getR(), config.getG(), config.getB(), config.getA());
        this.minAlphaVelocity = config.getMinAlphaVelocity();
        this.maxAlphaVelocity = config.getMaxAlphaVelocity();
        this.isAlphaFromZero = config.isAlphaFromZero();
        this.renderLayer = config.getRenderLayer();
        this.sourceSizeXMultiplier = config.getSourceSizeXMultiplier();
        this.sourceSizeYMultiplier = config.getSourceSizeYMultiplier();
        this.sourceVelocityXMultiplier = config.getSourceVelocityXMultiplier();
        this.sourceVelocityYMultiplier = config.getSourceVelocityYMultiplier();
        this.path = config.getPath();
        this.treeIndex = config.getTreeIndex();
        List<ConfigurableSound> effects = config.getSoundEffects();
        if (effects != null && effects.size() > 0) {
            this.soundEffects = new SoundEffect[effects.size()];
            for (int i = 0; i < effects.size(); i++) {
                ConfigurableSound soundEffect = effects.get(i);
                this.soundEffects[i] = new SoundEffect(Engine.assetsManager.getSound(PathHelper.convertPath(soundEffect.path())),
                        soundEffect.volume());
            }
        } else {
            this.soundEffects = null;
        }
    }

    public void init() {
        spawnRunnables.clear();

        if (spawnOverTime > 0) {
            spawnTime = 1.0 / spawnOverTime;
        } else {
            spawnTime = 0;
        }

        if (soundEffects != null && soundEffects.length > 0) {
            spawnRunnables.add((worldX, worldY, localX, localY, sizeX, sizeY, sin, cos, velocityX, velocityY, r, g, b, a,
                                updateLogic, lastValuesUpdateConsumer) -> {
                for (int i = 0; i < soundEffects.length; i++) {
                    SoundEffect soundEffect = soundEffects[i];
                    Engine.soundManager.play(soundEffect.soundBuffer(), soundEffect.volume(), worldX, worldY);
                }
            });
        }

        Supplier<Float> localXSupplier =
                minPosX == maxPosX ? () -> minPosX : () -> RandomHelper.randomFloat(rand, minPosX, maxPosX);
        Supplier<Float> localYSupplier =
                minPosY == maxPosY ? () -> minPosY : () -> RandomHelper.randomFloat(rand, minPosY, maxPosY);
        ParticleParamFunction velocityXFunc = makeFunction(minVelocityX, maxVelocityX, sourceVelocityXMultiplier);
        ParticleParamFunction velocityYFunc = makeFunction(minVelocityY, maxVelocityY, sourceVelocityYMultiplier);
        Supplier<Rotation> angleSupplier;

        if (minAngle == maxAngle) {
            Rotation rotation = new Rotation(minAngle);
            angleSupplier = () -> rotation;
        } else {
            Rotation rotation = new Rotation();
            angleSupplier = () -> {
                float angle = RandomHelper.randomFloat(rand, minAngle, maxAngle * MathUtils.TWO_PI);
                float sin = LUT.sin(angle);
                float cos = LUT.cos(angle);
                return rotation.set(sin, cos);
            };
        }
        Supplier<Float> angularVelocitySupplier = minAngularVelocity == maxAngularVelocity ? () -> minAngularVelocity :
                () -> RandomHelper.randomFloat(rand, minAngularVelocity, maxAngularVelocity);
        ParticleParamFunction sizeXFunc = makeFunction(minSizeX, maxSizeX, sourceSizeXMultiplier);
        ParticleParamFunction sizeYFunc = makeFunction(minSizeY, maxSizeY, sourceSizeYMultiplier);
        Supplier<Float> sizeVelocitySupplier = minSizeVelocity == maxSizeVelocity ? () -> minSizeVelocity :
                () -> RandomHelper.randomFloat(rand, minSizeVelocity, maxSizeVelocity);
        Supplier<Float> alphaVellocitySupplier = minAlphaVelocity == maxAlphaVelocity ? () -> minAlphaVelocity :
                () -> RandomHelper.randomFloat(rand, minAlphaVelocity, maxAlphaVelocity);
        long texture = textures.length > 0 ? textures[0].getTextureHandle() : 0;
        Supplier<Long> textureSupplier =
                textures.length > 1 ? () -> textures[rand.nextInt(textures.length)].getTextureHandle() : () -> texture;

        if (maxSpawnCount > minSpawnCount) {
            spawnRunnables.add((worldX, worldY, localX, localY, sizeX, sizeY, sin, cos, velocityX, velocityY, r, g, b, a,
                                updateLogic, lastValuesUpdateConsumer) -> {
                int spawnCount = rand.nextInt(maxSpawnCount - minSpawnCount + 1) + minSpawnCount;
                for (int i = 0; i < spawnCount; i++) {
                    Rotation rotation = angleSupplier.get();
                    float cos1 = cos * rotation.getCos() - sin * rotation.getSin();
                    float sin1 = sin * rotation.getCos() + cos * rotation.getSin();
                    PARTICLE_POOL.get().init(textureSupplier.get(), worldX + localXSupplier.get(), worldY + localYSupplier.get(),
                            localX, localY, velocityXFunc.apply(velocityX), velocityYFunc.apply(velocityY), sin1, cos1,
                            angularVelocitySupplier.get(), sizeXFunc.apply(sizeX), sizeYFunc.apply(sizeY),
                            sizeVelocitySupplier.get(), r * color.x, g * color.y, b * color.z, a * color.w,
                            alphaVellocitySupplier.get(), isAlphaFromZero, renderLayer, updateLogic, lastValuesUpdateConsumer);
                }
            });
        } else {
            if (minSpawnCount > 1) {
                spawnRunnables.add((worldX, worldY, localX, localY, sizeX, sizeY, sin, cos, velocityX, velocityY, r, g, b, a,
                                    updateLogic, lastValuesUpdateConsumer) -> {
                    for (int i = 0; i < minSpawnCount; i++) {
                        Rotation rotation = angleSupplier.get();
                        float cos1 = cos * rotation.getCos() - sin * rotation.getSin();
                        float sin1 = sin * rotation.getCos() + cos * rotation.getSin();
                        PARTICLE_POOL.get().init(textureSupplier.get(), worldX + localXSupplier.get(),
                                worldY + localYSupplier.get(), localX, localY, velocityXFunc.apply(velocityX),
                                velocityYFunc.apply(velocityY), sin1, cos1, angularVelocitySupplier.get(), sizeXFunc.apply(sizeX),
                                sizeYFunc.apply(sizeY), sizeVelocitySupplier.get(), r * color.x, g * color.y, b * color.z,
                                a * color.w, alphaVellocitySupplier.get(), isAlphaFromZero, renderLayer, updateLogic,
                                lastValuesUpdateConsumer);
                    }
                });
            } else if (minSpawnCount > 0) {
                spawnRunnables.add((worldX, worldY, localX, localY, sizeX, sizeY, sin, cos, velocityX, velocityY, r, g, b, a,
                                    updateLogic, lastValuesUpdateConsumer) -> {
                    Rotation rotation = angleSupplier.get();
                    float cos1 = cos * rotation.getCos() - sin * rotation.getSin();
                    float sin1 = sin * rotation.getCos() + cos * rotation.getSin();
                    PARTICLE_POOL.get().init(textureSupplier.get(), worldX + localXSupplier.get(), worldY + localYSupplier.get(),
                            localX, localY, velocityXFunc.apply(velocityX), velocityYFunc.apply(velocityY), sin1, cos1,
                            angularVelocitySupplier.get(), sizeXFunc.apply(sizeX), sizeYFunc.apply(sizeY),
                            sizeVelocitySupplier.get(), r * color.x, g * color.y, b * color.z, a * color.w,
                            alphaVellocitySupplier.get(), isAlphaFromZero, renderLayer, updateLogic, lastValuesUpdateConsumer);
                });
            }
        }

        if (childEffectsInstances.size() > 0) {
            for (int i = 0; i < childEffectsInstances.size(); i++) {
                ParticleEffect effect = childEffectsInstances.get(i);
                spawnRunnables.add((worldX, worldY, localX, localY, sizeX, sizeY, sin, cos, velocityX, velocityY, r, g, b, a,
                                    updateLogic, lastValuesUpdateConsumer) -> effect.play(worldX + localXSupplier.get(),
                        worldY + localYSupplier.get(), localX, localY, sizeXFunc.apply(sizeX), sizeYFunc.apply(sizeY), sin, cos,
                        velocityXFunc.apply(velocityX), velocityYFunc.apply(velocityY), r, g, b, a, updateLogic, lastValuesUpdateConsumer));
            }
        }
    }

    private ParticleParamFunction makeFunction(float minValue, float maxValue, float sourceMultiplayer) {
        if (sourceMultiplayer == 0.0f) {
            if (minValue == maxValue) {
                return value -> minValue;
            } else {
                return value -> RandomHelper.randomFloat(rand, minValue, maxValue);
            }
        } else if (sourceMultiplayer == 1.0f) {
            if (minValue == maxValue) {
                return value -> value + minValue;
            } else {
                return value -> value + RandomHelper.randomFloat(rand, minValue, maxValue);
            }
        } else {
            if (minValue == maxValue) {
                return value -> value * sourceMultiplayer + minValue;
            } else {
                return value -> value * sourceMultiplayer + RandomHelper.randomFloat(rand, minValue, maxValue);
            }
        }
    }

    public void emit(float x, float y, SpawnAccumulator spawnAccumulator) {
        emit(x, y, 0, 0, 0.0f, 1.0f, 0, 0, spawnAccumulator);
    }

    public void emit(float x, float y, float size, SpawnAccumulator spawnAccumulator) {
        emit(x, y, size, size, 0.0f, 1.0f, 0.0f, 0.0f, spawnAccumulator);
    }

    public void emit(float x, float y, float sizeX, float sizeY, float sin, float cos, float velocityX, float velocityY,
                     SpawnAccumulator spawnAccumulator) {
        emit(x, y, 0, 0, sizeX, sizeY, sin, cos, velocityX, velocityY, 1.0f, 1.0f, 1.0f, 1.0f, spawnAccumulator,
                Particle::defaultUpdateLogic, ParticleRender::defaultUpdateLastValues);
    }

    public void emit(float x, float y, float size, float sin, float cos, float velocityX, float velocityY, float r, float g,
                     float b, float a, SpawnAccumulator spawnAccumulator) {
        emit(x, y, 0, 0, size, size, sin, cos, velocityX, velocityY, r, g, b, a, spawnAccumulator, Particle::defaultUpdateLogic,
                ParticleRender::defaultUpdateLastValues);
    }

    public void emit(float x, float y, float sizeX, float sizeY, float sin, float cos,
                     float velocityX, float velocityY, float r, float g, float b, float a, SpawnAccumulator spawnAccumulator) {
        emit(x, y, 0, 0, sizeX, sizeY, sin, cos, velocityX, velocityY, r, g, b, a, spawnAccumulator,
                Particle::defaultUpdateLogic, ParticleRender::defaultUpdateLastValues);
    }

    public void emit(float x, float y, float localX, float localY, float sizeX, float sizeY, float sin, float cos,
                     float velocityX, float velocityY, float r, float g, float b, float a, SpawnAccumulator spawnAccumulator,
                     Consumer<Particle> updateLogic, Consumer<ParticleRender> lastValuesUpdateConsumer) {
        spawnAccumulator.update();
        int maxSpawnCount = 4;
        int count = 0;
        while (spawnAccumulator.getAccumulatedTime() >= spawnTime) {
            play(x, y, localX, localY, sizeX, sizeY, sin, cos, velocityX, velocityY, r, g, b, a,
                    updateLogic, lastValuesUpdateConsumer);
            spawnAccumulator.consume(spawnTime);
            count++;
            if (count == maxSpawnCount) {
                if (spawnAccumulator.getAccumulatedTime() >= spawnTime) {
                    int v = (int) Math.floor(spawnAccumulator.getAccumulatedTime() / spawnTime);
                    spawnAccumulator.consume(v * spawnTime);
                }

                break;
            }
        }
    }

    public void debug(float x, float y, float sizeX, float sizeY, float sin, float cos, float velocityX, float velocityY,
                      SpawnAccumulator spawnAccumulator) {
        removeDeadParticles();
        ParticleManager particleManager = Client.get().getParticleManager();
        int particlesCount = particleManager.getParticlesCount();

        if (spawnTime > 0) {
            emit(x, y, sizeX, sizeY, sin, cos, velocityX, velocityY, spawnAccumulator);
        } else {
            if (!isAlive()) {
                play(x, y, 0, 0, sizeX, sizeY, sin, cos, velocityX, velocityY, 1.0f, 1.0f, 1.0f, 1.0f,
                        Particle::defaultUpdateLogic, ParticleRender::defaultUpdateLastValues);
            }
        }

        int newParticlesCount = particleManager.getParticlesCount();
        if (newParticlesCount > particlesCount) {
            int count = newParticlesCount - particlesCount;
            for (int i = 0; i < count; i++) {
                aliveParticles.add(particleManager.getParticle(particlesCount + i));
            }
        }
    }

    public void play(float x, float y, float sizeX, float sizeY) {
        play(x, y, 0, 0, sizeX, sizeY, 0.0f, 1.0f, 0, 0, 1.0f, 1.0f, 1.0f, 1.0f, Particle::defaultUpdateLogic,
                ParticleRender::defaultUpdateLastValues);
    }

    public void play(float x, float y, float sizeX, float sizeY, float velocityX, float velocityY) {
        play(x, y, 0, 0, sizeX, sizeY, 0.0f, 1.0f, velocityX, velocityY, 1.0f, 1.0f, 1.0f, 1.0f, Particle::defaultUpdateLogic,
                ParticleRender::defaultUpdateLastValues);
    }

    public void play(float x, float y, float size, float r, float g, float b, float a) {
        play(x, y, 0, 0, size, size, 0.0f, 1.0f, 0, 0, r, g, b, a, Particle::defaultUpdateLogic, ParticleRender::defaultUpdateLastValues);
    }

    public void playSinCos(float x, float y, float size, float sin, float cos, float r, float g, float b, float a) {
        play(x, y, 0, 0, size, size, sin, cos, 0, 0, r, g, b, a, Particle::defaultUpdateLogic, ParticleRender::defaultUpdateLastValues);
    }

    public void play(float x, float y, float size, float velocityX, float velocityY, float r, float g, float b, float a) {
        play(x, y, 0, 0, size, size, 0.0f, 1.0f, velocityX, velocityY, r, g, b, a, Particle::defaultUpdateLogic,
                ParticleRender::defaultUpdateLastValues);
    }

    public void play(float x, float y, float localX, float localY, float sizeX, float sizeY, float sin, float cos,
                     float velocityX, float velocityY, float r, float g, float b, float a, Consumer<Particle> updateLogic) {
        play(x, y, localX, localY, sizeX, sizeY, sin, cos, velocityX, velocityY, r, g, b, a, updateLogic,
                ParticleRender::defaultUpdateLastValues);
    }

    public void play(float worldX, float worldY, float localX, float localY, float sizeX, float sizeY, float sin, float cos,
                     float velocityX, float velocityY, float r, float g, float b, float a, Consumer<Particle> updateLogic,
                     Consumer<ParticleRender> lastValuesUpdateConsumer) {
        for (int i = 0; i < spawnRunnables.size(); i++) {
            spawnRunnables.get(i).spawn(worldX, worldY, localX, localY, sizeX, sizeY, sin, cos, velocityX, velocityY, r, g, b, a,
                    updateLogic, lastValuesUpdateConsumer);
        }
    }

    private void removeDeadParticles() {
        for (int i = 0; i < aliveParticles.size(); i++) {
            if (aliveParticles.get(i).isDead()) {
                aliveParticles.remove(i--);
            }
        }
    }

    public void addChild(ParticleEffect particleEffect) {
        childEffectsInstances.add(particleEffect);
    }

    private boolean isAlive() {
        for (int i = 0; i < childEffectsInstances.size(); i++) {
            ParticleEffect effect = childEffectsInstances.get(i);
            effect.removeDeadParticles();
            if (effect.isAlive()) {
                return true;
            }
        }

        return aliveParticles.size() > 0;
    }

    String getPath() {
        return path.isEmpty() ? getFileName() : path + "/" + getFileName();
    }

    public void clearChildEffects() {
        childEffectsInstances.clear();
    }

    public void clear() {
        for (int i = 0; i < aliveParticles.size(); i++) {
            aliveParticles.get(i).setDead();
        }

        aliveParticles.clear();

        for (int i = 0; i < childEffectsInstances.size(); i++) {
            ParticleEffect effect = childEffectsInstances.get(i);
            effect.clear();
        }
    }
}