package net.bfsr.client.particle;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.core.Core;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.sound.SoundLoader;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.client.util.PathHelper;
import net.bfsr.config.Configurable;
import net.bfsr.math.MathUtils;
import net.bfsr.property.PropertiesHolder;
import net.bfsr.property.Property;
import net.bfsr.property.PropertyGuiElementType;
import net.bfsr.property.event.ChangeNameEventListener;
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.RandomHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@SuppressWarnings({"TransientFieldInNonSerializableClass", "UnnecessaryModifier"})
@Log4j2
@Setter
@Getter
public class ParticleEffect implements PropertiesHolder {
    @Configurable
    private String name;
    @Configurable
    @Property(elementType = PropertyGuiElementType.ARRAY, arrayElementType = PropertyGuiElementType.FILE_SELECTOR, arrayElementName = "texture")
    private List<String> texturePaths;
    @Configurable
    @Property
    private float spawnOverTime;
    @Configurable
    @Property(name = "spawnCount", fieldsAmount = 2)
    private int minSpawnCount, maxSpawnCount;
    @Configurable
    @Property(name = "position", fieldsAmount = 4)
    private float minPosX, minPosY, maxPosX, maxPosY;
    @Configurable
    @Property(name = "velocity", fieldsAmount = 4)
    private float minVelocityX, minVelocityY, maxVelocityX, maxVelocityY;
    @Configurable
    @Property(name = "angle", fieldsAmount = 2)
    private float minAngle, maxAngle;
    @Configurable
    @Property(name = "angularVelocity", fieldsAmount = 2)
    private float minAngularVelocity, maxAngularVelocity;
    @Configurable
    @Property(name = "size", fieldsAmount = 4)
    private float minSizeX, minSizeY, maxSizeX, maxSizeY;
    @Configurable
    @Property(name = "sizeVelocity", fieldsAmount = 2)
    private float minSizeVelocity, maxSizeVelocity;
    @Configurable
    @Property(name = "color", fieldsAmount = 4)
    private float r, g, b, a;
    @Configurable
    @Property(name = "alphaVelocity", fieldsAmount = 2)
    private float minAlphaVelocity, maxAlphaVelocity;
    @Configurable
    @Property(elementType = PropertyGuiElementType.CHECK_BOX)
    private boolean isAlphaFromZero;
    @Configurable
    @Property(elementType = PropertyGuiElementType.COMBO_BOX)
    private RenderLayer renderLayer;
    @Configurable
    @Property(elementType = PropertyGuiElementType.ARRAY)
    private List<ParticleSoundEffect> soundEffects;
    @Configurable
    @Property(elementType = PropertyGuiElementType.ARRAY)
    private List<ChildParticleEffect> childEffects;
    @Configurable
    @Property(name = "srcSizeMultiplayer", fieldsAmount = 2)
    private float sourceSizeXMultiplier, sourceSizeYMultiplier;
    @Configurable
    @Property(name = "srcVelocityMultiplayer", fieldsAmount = 2)
    private float sourceVelocityXMultiplier, sourceVelocityYMultiplier;
    @Configurable
    private String editorPath;

    private transient Texture[] textures;
    private transient float spawnTime;
    private transient long lastEmitTime;
    private transient long emitTime;
    private transient float accumulatedTime;
    private final transient List<Particle> aliveParticles = new ArrayList<>();

    private static final transient Random rand = new Random();

    private final transient List<ParticleEffectSpawnRunnable> spawnRunnables = new ArrayList<>();
    private final transient List<ParticleEffect> childEffectsInstances = new ArrayList<>();

    @FunctionalInterface
    private interface ParticleEffectSpawnRunnable {
        void spawn(float x, float y, float sizeX, float sizeY, float velocityX, float velocityY);
    }

    @FunctionalInterface
    private interface ParticleParamFunction {
        float apply(float value);
    }

    @Override
    public void setDefaultValues() {
        setName("Particle Effect");
        texturePaths = new ArrayList<>();
        texturePaths.add(TextureRegister.particleShipEngineBack.getPath());
        childEffects = new ArrayList<>();
        minSpawnCount = maxSpawnCount = 1;
        setColor(1.0f, 1.0f, 1.0f, 1.0f);
        setMinAlphaVelocity(0.5f);
        setMaxAlphaVelocity(0.5f);
        setMinSizeX(10.0f);
        setMinSizeY(10.0f);
        setMaxSizeX(10.0f);
        setMaxSizeY(10.0f);
        setRenderLayer(RenderLayer.DEFAULT_ADDITIVE);
        setSoundEffects(new ArrayList<>());
        setSourceSizeXMultiplier(1.0f);
        setSourceSizeYMultiplier(1.0f);
    }

    public void processDeprecated() {}

    public void init() {
        spawnRunnables.clear();

        if (spawnOverTime > 0) {
            if (spawnTime == 0) emitTime = System.currentTimeMillis();
            spawnTime = 1.0f / spawnOverTime;
        } else {
            spawnTime = 0;
        }

        textures = new Texture[texturePaths.size()];
        for (int i = 0; i < texturePaths.size(); i++) {
            textures[i] = TextureLoader.getTexture(PathHelper.convertPath(texturePaths.get(i)));
        }

        if (soundEffects != null && soundEffects.size() > 0) {
            for (int i = 0; i < soundEffects.size(); i++) {
                ParticleSoundEffect soundEffect = soundEffects.get(i);
                soundEffect.setSoundBuffer(SoundLoader.getBuffer(PathHelper.convertPath(soundEffect.getPath())));
            }

            spawnRunnables.add((x, y, sizeX, sizeY, velocityX, velocityY) -> {
                for (int i = 0; i < soundEffects.size(); i++) {
                    ParticleSoundEffect soundEffect = soundEffects.get(i);
                    Core.get().getSoundManager().play(new SoundSourceEffect(soundEffect.getSoundBuffer(), soundEffect.getVolume(), x, y));
                }
            });
        }

        Supplier<Float> localXSupplier = minPosX == maxPosX ? () -> minPosX : () -> RandomHelper.randomFloat(rand, minPosX, maxPosX);
        Supplier<Float> localYSupplier = minPosY == maxPosY ? () -> minPosY : () -> RandomHelper.randomFloat(rand, minPosY, maxPosY);
        ParticleParamFunction velocityXFunc = makeFunction(minVelocityX, maxVelocityX, sourceVelocityXMultiplier);
        ParticleParamFunction velocityYFunc = makeFunction(minVelocityY, maxVelocityY, sourceVelocityYMultiplier);
        Supplier<Float> angleSupplier = minAngle == maxAngle ? () -> minAngle : () -> RandomHelper.randomFloat(rand, minAngle, maxAngle * MathUtils.TWO_PI);
        Supplier<Float> angularVelocitySupplier = minAngularVelocity == maxAngularVelocity ? () -> minAngularVelocity : () -> RandomHelper.randomFloat(rand, minAngularVelocity, maxAngularVelocity);
        ParticleParamFunction sizeXFunc = makeFunction(minSizeX, maxSizeX, sourceSizeXMultiplier);
        ParticleParamFunction sizeYFunc = makeFunction(minSizeY, maxSizeY, sourceSizeYMultiplier);
        Supplier<Float> sizeVelocitySupplier = minSizeVelocity == maxSizeVelocity ? () -> minSizeVelocity : () -> RandomHelper.randomFloat(rand, minSizeVelocity, maxSizeVelocity);
        Supplier<Float> alphaVellocitySupplier = minAlphaVelocity == maxAlphaVelocity ? () -> minAlphaVelocity : () -> RandomHelper.randomFloat(rand, minAlphaVelocity, maxAlphaVelocity);
        long texture = textures.length > 0 ? textures[0].getTextureHandle() : 0;
        Supplier<Long> textureSupplier = textures.length > 1 ? () -> textures[rand.nextInt(textures.length)].getTextureHandle() : () -> texture;

        if (maxSpawnCount > minSpawnCount) {
            spawnRunnables.add((x, y, sizeX, sizeY, velocityX, velocityY) -> {
                int spawnCount = rand.nextInt(maxSpawnCount - minSpawnCount + 1) + minSpawnCount;
                for (int i = 0; i < spawnCount; i++) {
                    aliveParticles.add(ParticleSpawner.PARTICLE_POOL.getOrCreate(ParticleSpawner.PARTICLE_SUPPLIER).init(textureSupplier.get(), x + localXSupplier.get(), y + localYSupplier.get(),
                            velocityXFunc.apply(velocityX), velocityYFunc.apply(velocityY), angleSupplier.get(), angularVelocitySupplier.get(), sizeXFunc.apply(sizeX), sizeYFunc.apply(sizeY),
                            sizeVelocitySupplier.get(), r, g, b, a, alphaVellocitySupplier.get(), isAlphaFromZero, renderLayer));
                }
            });
        } else {
            if (minSpawnCount > 1) {
                spawnRunnables.add((x, y, sizeX, sizeY, velocityX, velocityY) -> {
                    for (int i = 0; i < minSpawnCount; i++) {
                        aliveParticles.add(ParticleSpawner.PARTICLE_POOL.getOrCreate(ParticleSpawner.PARTICLE_SUPPLIER).init(textureSupplier.get(), x + localXSupplier.get(), y + localYSupplier.get(),
                                velocityXFunc.apply(velocityX), velocityYFunc.apply(velocityY), angleSupplier.get(), angularVelocitySupplier.get(), sizeXFunc.apply(sizeX), sizeYFunc.apply(sizeY),
                                sizeVelocitySupplier.get(), r, g, b, a, alphaVellocitySupplier.get(), isAlphaFromZero, renderLayer));
                    }
                });
            } else if (minSpawnCount > 0) {
                spawnRunnables.add((x, y, sizeX, sizeY, velocityX, velocityY) -> aliveParticles.add(ParticleSpawner.PARTICLE_POOL.getOrCreate(ParticleSpawner.PARTICLE_SUPPLIER)
                        .init(textureSupplier.get(), x + localXSupplier.get(), y + localYSupplier.get(), velocityXFunc.apply(velocityX), velocityYFunc.apply(velocityY), angleSupplier.get(),
                                angularVelocitySupplier.get(), sizeXFunc.apply(sizeX), sizeYFunc.apply(sizeY), sizeVelocitySupplier.get(), r, g, b, a, alphaVellocitySupplier.get(),
                                isAlphaFromZero, renderLayer)));
            }
        }

        childEffectsInstances.clear();
        if (childEffects == null) childEffects = new ArrayList<>();
        if (childEffects.size() > 0) {
            for (int i = 0; i < childEffects.size(); i++) {
                ChildParticleEffect childParticleEffect = childEffects.get(i);
                ParticleEffect effect = ParticleEffectsRegistry.INSTANCE.getEffect(childParticleEffect.getName());
                if (effect != null) {
                    childEffectsInstances.add(effect);
                    int minSpawnCount = childParticleEffect.getMinSpawnCount();
                    int maxSpawnCount = childParticleEffect.getMaxSpawnCount();
                    float scale = childParticleEffect.getScale();
                    if (maxSpawnCount > minSpawnCount) {
                        spawnRunnables.add((x, y, sizeX, sizeY, velocityX, velocityY) -> {
                            int spawnCount = rand.nextInt(maxSpawnCount - minSpawnCount + 1) + minSpawnCount;
                            for (int j = 0; j < spawnCount; j++) {
                                effect.play(x + localXSupplier.get(), y + localYSupplier.get(), sizeXFunc.apply(sizeX) * scale,
                                        sizeYFunc.apply(sizeY) * scale, velocityXFunc.apply(velocityX), velocityYFunc.apply(velocityY));
                            }
                        });
                    } else {
                        if (minSpawnCount > 1) {
                            spawnRunnables.add((x, y, sizeX, sizeY, velocityX, velocityY) -> {
                                for (int j = 0; j < minSpawnCount; j++) {
                                    effect.play(x + localXSupplier.get(), y + localYSupplier.get(), sizeXFunc.apply(sizeX) * scale,
                                            sizeYFunc.apply(sizeY) * scale, velocityXFunc.apply(velocityX), velocityYFunc.apply(velocityY));
                                }
                            });
                        } else if (minSpawnCount > 0) {
                            spawnRunnables.add((x, y, sizeX, sizeY, velocityX, velocityY) -> effect.play(x + localXSupplier.get(), y + localYSupplier.get(), sizeXFunc.apply(sizeX) * scale,
                                    sizeYFunc.apply(sizeY) * scale, velocityXFunc.apply(velocityX), velocityYFunc.apply(velocityY)));
                        }
                    }
                } else {
                    log.error("Couldn't find particle effect {}", childParticleEffect.getName());
                }
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

    public void create() {
        emitTime = System.currentTimeMillis();
    }

    public void emit(float x, float y, float sizeX, float sizeY, float velocityX, float velocityY) {
        checkParticles();

        if (spawnTime > 0) {
            lastEmitTime = emitTime;
            emitTime = System.currentTimeMillis();
            accumulatedTime += (emitTime - lastEmitTime) / 1000.0f;
            while (accumulatedTime > spawnTime) {
                play(x, y, sizeX, sizeY, velocityX, velocityY);
                accumulatedTime -= spawnTime;
            }
        } else {
            if (!isAlive()) {
                play(x, y, sizeX, sizeY, velocityX, velocityY);
            }
        }
    }

    public void play(float x, float y, float sizeX, float sizeY) {
        play(x, y, sizeX, sizeY, 0, 0);
    }

    public void play(float x, float y, float sizeX, float sizeY, float velocityX, float velocityY) {
        for (int i = 0; i < spawnRunnables.size(); i++) {
            spawnRunnables.get(i).spawn(x, y, sizeX, sizeY, velocityX, velocityY);
        }
    }

    public void setColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    private void checkParticles() {
        for (int i = 0; i < aliveParticles.size(); i++) {
            if (aliveParticles.get(i).isDead()) {
                aliveParticles.remove(i--);
            }
        }
    }

    @Override
    public void registerChangeNameEventListener(ChangeNameEventListener listener) {

    }

    @Override
    public void clearListeners() {

    }

    public boolean isAlive() {
        for (int i = 0; i < childEffectsInstances.size(); i++) {
            ParticleEffect effect = childEffectsInstances.get(i);
            effect.checkParticles();
            if (effect.isAlive()) {
                return true;
            }
        }

        return aliveParticles.size() > 0;
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