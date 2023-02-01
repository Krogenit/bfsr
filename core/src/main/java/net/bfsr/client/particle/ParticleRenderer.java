package net.bfsr.client.particle;

import lombok.Getter;
import net.bfsr.client.model.TexturedQuad;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.ParticleInstancedShader;
import net.bfsr.core.Core;
import net.bfsr.settings.EnumOption;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.*;

public class ParticleRenderer {
    @Getter
    private static ParticleRenderer instance;
    public static final RenderLayer[] POSITION_TYPES = RenderLayer.values();
    private static final int INSTANCE_DATA_LENGTH = 22;
    private final int parallelism = Runtime.getRuntime().availableProcessors();
    private final boolean multithreadingSupported = parallelism > 1;

    private final BaseShader defaultShader;
    @Getter
    private final ParticleInstancedShader particleShader = new ParticleInstancedShader();

    private final EnumMap<RenderLayer, List<Particle>> particlesHashMap = new EnumMap<>(RenderLayer.class);
    @Getter
    private final List<Particle> particles = new ArrayList<>();
    private final List<Particle> backgroundAlphaParticles = new ArrayList<>(256);
    private final List<Particle> backgroundAdditiveParticles = new ArrayList<>(256);
    private final List<Particle> alphaParticles = new ArrayList<>(256);
    private final List<Particle> additiveParticles = new ArrayList<>(256);
    @Getter
    private final List<ParticleWreck> particlesWrecks = new ArrayList<>();

    private TexturedQuad quad;
    private final ByteBuffer[] buffers = new ByteBuffer[4];
    private ParticlesStoreTask[] particlesStoreTasks;
    private ExecutorService executorService;
    private Future<?>[] taskFutures;
    private int particleWreckEffects;
    private int taskCount;

    public ParticleRenderer(BaseShader baseShader) {
        this.defaultShader = baseShader;

        buffers[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()] = BufferUtils.createByteBuffer(INSTANCE_DATA_LENGTH * 4 * 4096);
        buffers[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()] = BufferUtils.createByteBuffer(INSTANCE_DATA_LENGTH * 4 * 4096);
        buffers[RenderLayer.BACKGROUND_ADDITIVE.ordinal()] = BufferUtils.createByteBuffer(INSTANCE_DATA_LENGTH * 4 * 4096);
        buffers[RenderLayer.DEFAULT_ADDITIVE.ordinal()] = BufferUtils.createByteBuffer(INSTANCE_DATA_LENGTH * 4 * 4096);

        particlesHashMap.put(RenderLayer.BACKGROUND_ALPHA_BLENDED, backgroundAlphaParticles);
        particlesHashMap.put(RenderLayer.BACKGROUND_ADDITIVE, backgroundAdditiveParticles);
        particlesHashMap.put(RenderLayer.DEFAULT_ALPHA_BLENDED, alphaParticles);
        particlesHashMap.put(RenderLayer.DEFAULT_ADDITIVE, additiveParticles);

        if (multithreadingSupported) {
            taskFutures = new Future[parallelism];
            particlesStoreTasks = new ParticlesStoreTask[parallelism];
            executorService = Executors.newFixedThreadPool(parallelism);
            for (int i = 0; i < particlesStoreTasks.length; i++) {
                particlesStoreTasks[i] = new ParticlesStoreTask();
                particlesStoreTasks[i].init(particlesWrecks, backgroundAlphaParticles, backgroundAdditiveParticles, alphaParticles, additiveParticles, buffers);
            }
        }

        instance = this;
    }

    public void init() {
        this.particleShader.load();
        this.particleShader.init();

        quad = TexturedQuad.createParticleCenteredQuad();
        quad.addInstancedAttribute(2, 1, 4, INSTANCE_DATA_LENGTH, 0);
        quad.addInstancedAttribute(2, 2, 4, INSTANCE_DATA_LENGTH, 4);
        quad.addInstancedAttribute(2, 3, 4, INSTANCE_DATA_LENGTH, 8);
        quad.addInstancedAttribute(2, 4, 4, INSTANCE_DATA_LENGTH, 12);
        quad.addInstancedAttribute(2, 5, 4, INSTANCE_DATA_LENGTH, 16);
        quad.addInstancedAttribute(2, 6, 2, INSTANCE_DATA_LENGTH, 20);
    }

    public void update() {
        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            particle.update();
            if (particle.isDead()) {
                removeParticle(particle, i);
                i--;
            }
        }

        for (int i = 0; i < particlesWrecks.size(); i++) {
            ParticleWreck particle = particlesWrecks.get(i);
            particle.update();
            if (particle.isDead()) {
                removeParticle(particle);
                i--;
            }
        }
    }

    private void checkBufferSizeAndClear() {
        for (int i = 0; i < POSITION_TYPES.length; i++) {
            RenderLayer positionType = POSITION_TYPES[i];
            int newDataSize = 0;

            if (positionType == RenderLayer.DEFAULT_ALPHA_BLENDED) {
                newDataSize += particlesWrecks.size() * INSTANCE_DATA_LENGTH * 4;
            } else if (positionType == RenderLayer.DEFAULT_ADDITIVE) {
                newDataSize += particlesWrecks.size() * INSTANCE_DATA_LENGTH * 4 * 2;
            }

            List<Particle> particles = particlesHashMap.get(positionType);
            newDataSize += particles.size() * INSTANCE_DATA_LENGTH * 4;

            ByteBuffer buffer = buffers[positionType.ordinal()];
            while (buffer.capacity() < newDataSize) {
                buffer = BufferUtils.createByteBuffer(buffer.capacity() << 1);
                buffers[positionType.ordinal()] = buffer;
                if (multithreadingSupported) {
                    for (int i1 = 0; i1 < particlesStoreTasks.length; i1++) {
                        particlesStoreTasks[i1].init(particlesWrecks, backgroundAlphaParticles, backgroundAdditiveParticles, alphaParticles, additiveParticles, buffers);
                    }
                }
            }

            buffer.clear();
        }
    }

    public void storeParticlesToBuffers(float interpolation) {
        checkBufferSizeAndClear();

        int totalParticles = particles.size() + particlesWrecks.size();
        int multithreadedThreshold = 2048;
        int particlesByTask = 2048;

        boolean multithreaded = multithreadingSupported && totalParticles >= multithreadedThreshold;
        taskCount = multithreaded ? (int) Math.ceil(Math.min(totalParticles / (float) particlesByTask, parallelism)) : 1;

        particleWreckEffects = calculateParticleWreckEffects();

        int backgroundAlphaBufferIndex = 0;
        int backgroundAdditiveBufferIndex = 0;
        int alphaBufferIndex = 0;
        int additiveBufferIndex = 0;

        int particleWreckPerTask = (int) Math.ceil(particlesWrecks.size() / (float) taskCount);
        int shipWrecksStartIndex = 0, shipWrecksEndIndex = particleWreckPerTask;
        int particleWreckEffectsPerTask = (int) Math.ceil(particleWreckEffects / (float) taskCount);
        int shipWrecksEffectsStartIndex = 0, shipWrecksEffectsEndIndex = particleWreckEffectsPerTask;

        int backgroundAlphaParticlesPerTask = (int) Math.ceil(backgroundAlphaParticles.size() / (float) taskCount);
        int backgroundAlphaParticlesStartIndex = 0, backgroundAlphaParticlesEndIndex = backgroundAlphaParticlesPerTask;
        int backgroundAdditiveParticlesPerTask = (int) Math.ceil(backgroundAdditiveParticles.size() / (float) taskCount);
        int backgroundAdditiveParticlesStartIndex = 0, backgroundAdditiveParticlesEndIndex = backgroundAdditiveParticlesPerTask;
        int alphaParticlesPerTask = (int) Math.ceil(alphaParticles.size() / (float) taskCount);
        int alphaParticlesStartIndex = 0, alphaParticlesEndIndex = alphaParticlesPerTask;
        int additiveParticlesPerTask = (int) Math.ceil(additiveParticles.size() / (float) taskCount);
        int additiveParticlesStartIndex = 0, additiveParticlesEndIndex = additiveParticlesPerTask;

        for (int i = 0; i < taskCount; i++) {
            ParticlesStoreTask particlesStoreTask = particlesStoreTasks[i];

            particlesStoreTask.update(interpolation, backgroundAlphaBufferIndex, backgroundAdditiveBufferIndex, alphaBufferIndex, additiveBufferIndex, shipWrecksStartIndex,
                    shipWrecksEndIndex, shipWrecksEffectsStartIndex, shipWrecksEffectsEndIndex, backgroundAlphaParticlesStartIndex, backgroundAlphaParticlesEndIndex,
                    backgroundAdditiveParticlesStartIndex, backgroundAdditiveParticlesEndIndex, alphaParticlesStartIndex, alphaParticlesEndIndex, additiveParticlesStartIndex,
                    additiveParticlesEndIndex);
            if (multithreaded) {
                taskFutures[i] = executorService.submit(particlesStoreTask);

                backgroundAlphaBufferIndex += backgroundAlphaParticlesPerTask * INSTANCE_DATA_LENGTH * 4;
                backgroundAdditiveBufferIndex += backgroundAdditiveParticlesPerTask * INSTANCE_DATA_LENGTH * 4;
                alphaBufferIndex += (alphaParticlesPerTask + particleWreckPerTask) * INSTANCE_DATA_LENGTH * 4;
                additiveBufferIndex += (additiveParticlesPerTask + particleWreckEffectsPerTask) * INSTANCE_DATA_LENGTH * 4;

                shipWrecksStartIndex += particleWreckPerTask;
                shipWrecksEndIndex += particleWreckPerTask;
                shipWrecksEndIndex = Math.min(shipWrecksEndIndex, particlesWrecks.size());

                shipWrecksEffectsStartIndex += particleWreckEffectsPerTask;
                shipWrecksEffectsEndIndex += particleWreckEffectsPerTask;
                shipWrecksEffectsEndIndex = Math.min(shipWrecksEffectsEndIndex, particleWreckEffects);

                backgroundAlphaParticlesStartIndex += backgroundAlphaParticlesPerTask;
                backgroundAlphaParticlesEndIndex += backgroundAlphaParticlesPerTask;
                backgroundAlphaParticlesEndIndex = Math.min(backgroundAlphaParticlesEndIndex, backgroundAlphaParticles.size());

                backgroundAdditiveParticlesStartIndex += backgroundAdditiveParticlesPerTask;
                backgroundAdditiveParticlesEndIndex += backgroundAdditiveParticlesPerTask;
                backgroundAdditiveParticlesEndIndex = Math.min(backgroundAdditiveParticlesEndIndex, backgroundAdditiveParticles.size());

                alphaParticlesStartIndex += alphaParticlesPerTask;
                alphaParticlesEndIndex += alphaParticlesPerTask;
                alphaParticlesEndIndex = Math.min(alphaParticlesEndIndex, alphaParticles.size());

                additiveParticlesStartIndex += additiveParticlesPerTask;
                additiveParticlesEndIndex += additiveParticlesPerTask;
                additiveParticlesEndIndex = Math.min(additiveParticlesEndIndex, additiveParticles.size());
            } else {
                particlesStoreTask.run();
                taskCount = 0;
            }
        }
    }

    private int calculateParticleWreckEffects() {
        int count = 0;

        int size = particlesWrecks.size();
        for (int i = 0; i < size; i++) {
            ParticleWreck particleWreck = particlesWrecks.get(i);
            if (particleWreck.getColorFire() != null && particleWreck.getColorFire().w > 0) {
                count++;
            }

            if (particleWreck.getColorLight() != null && particleWreck.getColorLight().w > 0) {
                count++;
            }
        }

        return count;
    }

    public void waitTasks() {
        if (taskCount > 0) {
            try {
                for (int i = 0; i < taskCount; i++) {
                    taskFutures[i].get();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void renderBackground() {
        particleShader.enable();

        ByteBuffer buffer = buffers[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()];
        int count = backgroundAlphaParticles.size();

        if (count > 0) {
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            buffer.limit(count * INSTANCE_DATA_LENGTH * 4);
            render(count, buffer);
        }

        buffer = buffers[RenderLayer.BACKGROUND_ADDITIVE.ordinal()];
        count = backgroundAdditiveParticles.size();

        if (count > 0) {
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            buffer.limit(count * INSTANCE_DATA_LENGTH * 4);
            render(count, buffer);
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    public void render() {
        particleShader.enable();

        ByteBuffer buffer = buffers[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()];
        int count = alphaParticles.size() + particlesWrecks.size();

        if (count > 0) {
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            buffer.limit(count * INSTANCE_DATA_LENGTH * 4);
            render(count, buffer);
        }

        if (EnumOption.SHOW_DEBUG_BOXES.getBoolean()) {
            Core.getCore().getRenderer().getCamera().setupOpenGLMatrix();

            GL20.glUseProgram(0);
            for (int i = 0; i < particlesWrecks.size(); i++) {
                ParticleWreck particle = particlesWrecks.get(i);
                particle.renderDebug();
            }
            defaultShader.enable();
        }

        buffer = buffers[RenderLayer.DEFAULT_ADDITIVE.ordinal()];
        count = additiveParticles.size() + particleWreckEffects;

        if (count > 0) {
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            buffer.limit(count * INSTANCE_DATA_LENGTH * 4);
            render(count, buffer);
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    private void render(int count, ByteBuffer buffer) {
        quad.updateVertexBuffer(2, buffer, INSTANCE_DATA_LENGTH);
        GL30C.glBindVertexArray(quad.getVaoId());
        GL31C.glDrawArraysInstanced(GL11C.GL_QUADS, 0, 4, count);
        Core.getCore().getRenderer().increaseDrawCalls();
    }

    void addParticle(ParticleWreck particle) {
        particlesWrecks.add(particle);
    }

    void addParticle(Particle particle) {
        List<Particle> particles = particlesHashMap.get(particle.getRenderLayer());

        particles.add(particle);
        this.particles.add(particle);
    }

    private void removeParticle(ParticleWreck particle) {
        Core.getCore().getWorld().removeDynamicParticle(particle);
        particlesWrecks.remove(particle);
        particle.returnToPool();
    }

    private void removeParticle(Particle particle, int index) {
        particles.remove(index);
        particle.returnToPool();

        List<Particle> particles = particlesHashMap.get(particle.getRenderLayer());
        particles.remove(particle);
    }

    public void onExitToMainMenu() {
        for (int i = 0; i < particles.size(); i++) {
            particles.get(i).returnToPool();
        }
        particles.clear();
        for (int i = 0; i < particlesWrecks.size(); i++) {
            particlesWrecks.get(i).returnToPool();
        }
        particlesWrecks.clear();
        particlesHashMap.values().forEach(List::clear);
    }

    public void clear() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
