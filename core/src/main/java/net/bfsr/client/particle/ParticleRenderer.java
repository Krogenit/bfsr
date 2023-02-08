package net.bfsr.client.particle;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.render.InstancedRenderer;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.core.Core;
import net.bfsr.settings.EnumOption;
import net.bfsr.util.MulthithreadingUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Log4j2
public class ParticleRenderer {
    public static final RenderLayer[] RENDER_LAYERS = RenderLayer.values();
    private static final int START_PARTICLE_COUNT = 8192;

    private final List<Particle>[] particlesByRenderLayer = new List[4];
    @Getter
    private final List<ParticleWreck> particlesWrecks = new ArrayList<>();

    private final ByteBuffer[] materialBuffers = new ByteBuffer[4];
    private final FloatBuffer[] vertexBuffers = new FloatBuffer[4];
    private ParticlesStoreTask[] particlesStoreTasks;
    private ExecutorService executorService;
    private Future<?>[] taskFutures;
    private int particleWreckEffects;
    @Getter
    private int taskCount;

    public ParticleRenderer() {
        for (int i = 0; i < RENDER_LAYERS.length; i++) {
            materialBuffers[RENDER_LAYERS[i].ordinal()] = BufferUtils.createByteBuffer(START_PARTICLE_COUNT << 5);
            vertexBuffers[RENDER_LAYERS[i].ordinal()] = BufferUtils.createFloatBuffer(START_PARTICLE_COUNT << 4);
            particlesByRenderLayer[RENDER_LAYERS[i].ordinal()] = new ArrayList<>(256);
        }

        if (MulthithreadingUtils.MULTITHREADING_SUPPORTED) {
            taskFutures = new Future[MulthithreadingUtils.PARALLELISM];
            particlesStoreTasks = new ParticlesStoreTask[MulthithreadingUtils.PARALLELISM];
            executorService = Executors.newFixedThreadPool(MulthithreadingUtils.PARALLELISM);
            for (int i = 0; i < particlesStoreTasks.length; i++) {
                particlesStoreTasks[i] = new ParticlesStoreTask();
                particlesStoreTasks[i].init(particlesWrecks, particlesByRenderLayer, vertexBuffers, materialBuffers);
            }
        }
    }

    public void init() {}

    public void update() {
        for (int i = 0; i < particlesByRenderLayer.length; i++) {
            List<Particle> particles = particlesByRenderLayer[i];
            for (int i1 = 0; i1 < particles.size(); i1++) {
                Particle particle = particles.get(i1);
                particle.update();
                if (particle.isDead()) {
                    particle.returnToPool();
                    particles.remove(i1--);
                }
            }
        }

        for (int i = 0; i < particlesWrecks.size(); i++) {
            ParticleWreck particle = particlesWrecks.get(i);
            particle.update();
            if (particle.isDead()) {
                removeParticle(particle, i--);
            }
        }
    }

    private void checkBufferSizeAndClear() {
        for (int i = 0; i < RENDER_LAYERS.length; i++) {
            RenderLayer renderLayer = RENDER_LAYERS[i];
            int newDataSize = 0;

            if (renderLayer == RenderLayer.DEFAULT_ALPHA_BLENDED) {
                newDataSize += particlesWrecks.size();
            } else if (renderLayer == RenderLayer.DEFAULT_ADDITIVE) {
                newDataSize += particlesWrecks.size() << 1;
            }

            List<Particle> particles = particlesByRenderLayer[renderLayer.ordinal()];
            newDataSize += particles.size();

            ByteBuffer buffer = materialBuffers[renderLayer.ordinal()];
            while (buffer.capacity() < newDataSize * 8 * 4) {
                buffer = BufferUtils.createByteBuffer(buffer.capacity() << 1);
                materialBuffers[renderLayer.ordinal()] = buffer;
                if (MulthithreadingUtils.MULTITHREADING_SUPPORTED) {
                    for (int i1 = 0; i1 < particlesStoreTasks.length; i1++) {
                        particlesStoreTasks[i1].init(particlesWrecks, particlesByRenderLayer, vertexBuffers, materialBuffers);
                    }
                }
            }

            buffer.clear();

            FloatBuffer vertexBuffer = vertexBuffers[renderLayer.ordinal()];
            while (vertexBuffer.capacity() < newDataSize << 4) {
                vertexBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity() << 1);
                vertexBuffers[renderLayer.ordinal()] = vertexBuffer;
                if (MulthithreadingUtils.MULTITHREADING_SUPPORTED) {
                    for (int i1 = 0; i1 < particlesStoreTasks.length; i1++) {
                        particlesStoreTasks[i1].init(particlesWrecks, particlesByRenderLayer, vertexBuffers, materialBuffers);
                    }
                }
            }

            vertexBuffer.clear();
        }
    }

    public void storeParticlesToBuffers(float interpolation) {
        checkBufferSizeAndClear();

        int totalParticles = getParticlesCount() + particlesWrecks.size();
        int multithreadedThreshold = 2048;
        int particlesByTask = 2048;

        boolean multithreaded = MulthithreadingUtils.MULTITHREADING_SUPPORTED && totalParticles >= multithreadedThreshold;
        taskCount = multithreaded ? (int) Math.ceil(Math.min(totalParticles / (float) particlesByTask, MulthithreadingUtils.PARALLELISM)) : 1;

        particleWreckEffects = calculateParticleWreckEffects();

        int backgroundAlphaBufferIndex = 0;
        int backgroundAdditiveBufferIndex = 0;
        int alphaBufferIndex = 0;
        int additiveBufferIndex = 0;

        int particleWreckPerTask = (int) Math.ceil(particlesWrecks.size() / (float) taskCount);
        int shipWrecksStartIndex = 0, shipWrecksEndIndex = particleWreckPerTask;
        int particleWreckEffectsPerTask = (int) Math.ceil(particleWreckEffects / (float) taskCount);
        int shipWrecksEffectsStartIndex = 0, shipWrecksEffectsEndIndex = particleWreckEffectsPerTask;

        int backgroundAlphaParticlesPerTask = (int) Math.ceil(getParticles(RenderLayer.BACKGROUND_ALPHA_BLENDED).size() / (float) taskCount);
        int backgroundAlphaParticlesStartIndex = 0, backgroundAlphaParticlesEndIndex = backgroundAlphaParticlesPerTask;
        int backgroundAdditiveParticlesPerTask = (int) Math.ceil(getParticles(RenderLayer.BACKGROUND_ADDITIVE).size() / (float) taskCount);
        int backgroundAdditiveParticlesStartIndex = 0, backgroundAdditiveParticlesEndIndex = backgroundAdditiveParticlesPerTask;
        int alphaParticlesPerTask = (int) Math.ceil(getParticles(RenderLayer.DEFAULT_ALPHA_BLENDED).size() / (float) taskCount);
        int alphaParticlesStartIndex = 0, alphaParticlesEndIndex = alphaParticlesPerTask;
        int additiveParticlesPerTask = (int) Math.ceil(getParticles(RenderLayer.DEFAULT_ADDITIVE).size() / (float) taskCount);
        int additiveParticlesStartIndex = 0, additiveParticlesEndIndex = additiveParticlesPerTask;

        for (int i = 0; i < taskCount; i++) {
            ParticlesStoreTask particlesStoreTask = particlesStoreTasks[i];

            particlesStoreTask.update(interpolation, backgroundAlphaBufferIndex, backgroundAdditiveBufferIndex, alphaBufferIndex, additiveBufferIndex, shipWrecksStartIndex,
                    shipWrecksEndIndex, shipWrecksEffectsStartIndex, shipWrecksEffectsEndIndex, backgroundAlphaParticlesStartIndex, backgroundAlphaParticlesEndIndex,
                    backgroundAdditiveParticlesStartIndex, backgroundAdditiveParticlesEndIndex, alphaParticlesStartIndex, alphaParticlesEndIndex, additiveParticlesStartIndex,
                    additiveParticlesEndIndex);
            if (multithreaded) {
                taskFutures[i] = executorService.submit(particlesStoreTask);

                backgroundAlphaBufferIndex += backgroundAlphaParticlesPerTask << 4;
                backgroundAdditiveBufferIndex += backgroundAdditiveParticlesPerTask << 4;
                alphaBufferIndex += (alphaParticlesPerTask + particleWreckPerTask) << 4;
                additiveBufferIndex += (additiveParticlesPerTask + particleWreckEffectsPerTask) << 4;

                shipWrecksStartIndex += particleWreckPerTask;
                shipWrecksEndIndex += particleWreckPerTask;
                shipWrecksEndIndex = Math.min(shipWrecksEndIndex, particlesWrecks.size());

                shipWrecksEffectsStartIndex += particleWreckEffectsPerTask;
                shipWrecksEffectsEndIndex += particleWreckEffectsPerTask;
                shipWrecksEffectsEndIndex = Math.min(shipWrecksEffectsEndIndex, particleWreckEffects);

                backgroundAlphaParticlesStartIndex += backgroundAlphaParticlesPerTask;
                backgroundAlphaParticlesEndIndex += backgroundAlphaParticlesPerTask;
                backgroundAlphaParticlesEndIndex = Math.min(backgroundAlphaParticlesEndIndex, getParticles(RenderLayer.BACKGROUND_ALPHA_BLENDED).size());

                backgroundAdditiveParticlesStartIndex += backgroundAdditiveParticlesPerTask;
                backgroundAdditiveParticlesEndIndex += backgroundAdditiveParticlesPerTask;
                backgroundAdditiveParticlesEndIndex = Math.min(backgroundAdditiveParticlesEndIndex, getParticles(RenderLayer.BACKGROUND_ADDITIVE).size());

                alphaParticlesStartIndex += alphaParticlesPerTask;
                alphaParticlesEndIndex += alphaParticlesPerTask;
                alphaParticlesEndIndex = Math.min(alphaParticlesEndIndex, getParticles(RenderLayer.DEFAULT_ALPHA_BLENDED).size());

                additiveParticlesStartIndex += additiveParticlesPerTask;
                additiveParticlesEndIndex += additiveParticlesPerTask;
                additiveParticlesEndIndex = Math.min(additiveParticlesEndIndex, getParticles(RenderLayer.DEFAULT_ADDITIVE).size());
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
                log.error("Error occurred during particle tasks sync", e);
            }
        }
    }

    public void renderBackground() {
        FloatBuffer vertexBuffer = vertexBuffers[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()];
        ByteBuffer materialBuffer = materialBuffers[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()];
        int count = particlesByRenderLayer[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()].size();

        if (count > 0) {
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            vertexBuffer.limit(count << 4);
            materialBuffer.limit(count << 5);
            render(count, vertexBuffer, materialBuffer);
        }

        vertexBuffer = vertexBuffers[RenderLayer.BACKGROUND_ADDITIVE.ordinal()];
        materialBuffer = materialBuffers[RenderLayer.BACKGROUND_ADDITIVE.ordinal()];
        count = particlesByRenderLayer[RenderLayer.BACKGROUND_ADDITIVE.ordinal()].size();

        if (count > 0) {
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            vertexBuffer.limit(count << 4);
            materialBuffer.limit(count << 5);
            render(count, vertexBuffer, materialBuffer);
        }
    }

    public void render() {
        FloatBuffer vertexBuffer = vertexBuffers[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()];
        ByteBuffer materialBuffer = materialBuffers[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()];
        int count = particlesByRenderLayer[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()].size() + particlesWrecks.size();

        if (count > 0) {
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            render(count, vertexBuffer, materialBuffer);
        }

        if (EnumOption.SHOW_DEBUG_BOXES.getBoolean()) {
            Core.getCore().getRenderer().getCamera().setupOpenGLMatrix();

            GL20.glUseProgram(0);
            for (int i = 0; i < particlesWrecks.size(); i++) {
                ParticleWreck particle = particlesWrecks.get(i);
                particle.renderDebug();
            }
            Core.getCore().getRenderer().getShader().enable();
        }

        vertexBuffer = vertexBuffers[RenderLayer.DEFAULT_ADDITIVE.ordinal()];
        materialBuffer = materialBuffers[RenderLayer.DEFAULT_ADDITIVE.ordinal()];
        count = particlesByRenderLayer[RenderLayer.DEFAULT_ADDITIVE.ordinal()].size() + particleWreckEffects;

        if (count > 0) {
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            render(count, vertexBuffer, materialBuffer);
        }
    }

    private void render(int count, FloatBuffer vertexBuffer, ByteBuffer materialBuffer) {
        InstancedRenderer.INSTANCE.render(count, vertexBuffer, materialBuffer);
    }

    void addParticle(ParticleWreck particle) {
        particlesWrecks.add(particle);
    }

    void addParticle(Particle particle) {
        getParticles(particle.getRenderLayer()).add(particle);
    }

    private void removeParticle(ParticleWreck particle, int index) {
        Core.getCore().getWorld().removeDynamicParticle(particle);
        particlesWrecks.remove(index);
        particle.returnToPool();
    }

    public void onExitToMainMenu() {
        for (int i = 0; i < particlesWrecks.size(); i++) {
            particlesWrecks.get(i).returnToPool();
        }
        particlesWrecks.clear();
        for (int i = 0; i < particlesByRenderLayer.length; i++) {
            List<Particle> particles = particlesByRenderLayer[i];
            for (int i1 = 0; i1 < particles.size(); i1++) {
                particles.get(i1).returnToPool();
            }
            particles.clear();
        }
    }

    public void clear() {
        if (executorService != null) {
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

    private List<Particle> getParticles(RenderLayer renderLayer) {
        return particlesByRenderLayer[renderLayer.ordinal()];
    }

    public int getParticlesCount() {
        int count = 0;
        for (int i = 0; i < particlesByRenderLayer.length; i++) {
            count += particlesByRenderLayer[i].size();
        }

        return count;
    }

    public void postPhysicsUpdate() {
        for (int i = 0, size = particlesWrecks.size(); i < size; i++) {
            particlesWrecks.get(i).postPhysicsUpdate();
        }
    }
}
