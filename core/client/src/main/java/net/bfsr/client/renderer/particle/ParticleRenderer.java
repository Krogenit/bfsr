package net.bfsr.client.renderer.particle;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.core.Core;
import net.bfsr.client.particle.Particle;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.renderer.OpenGLHelper;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.util.MultithreadingUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Log4j2
public class ParticleRenderer {
    public static final RenderLayer[] RENDER_LAYERS = RenderLayer.values();
    private static final int START_PARTICLE_COUNT = 8192;

    private final List<Particle>[] particlesByRenderLayer = new List[4];
    private final ByteBuffer[] materialBuffers = new ByteBuffer[4];
    private final FloatBuffer[] vertexBuffers = new FloatBuffer[4];
    private ParticlesStoreTask[] particlesStoreTasks;
    private ParticlesStoreTask[] backgroundParticlesStoreTasks;
    private Future<?>[] taskFutures;
    private Future<?>[] backgroundTaskFutures;
    @Getter
    private int taskCount;
    private boolean multithreaded;

    public ParticleRenderer() {
        for (int i = 0; i < RENDER_LAYERS.length; i++) {
            materialBuffers[RENDER_LAYERS[i].ordinal()] = BufferUtils.createByteBuffer(START_PARTICLE_COUNT << 5);
            vertexBuffers[RENDER_LAYERS[i].ordinal()] = BufferUtils.createFloatBuffer(START_PARTICLE_COUNT << 4);
            particlesByRenderLayer[RENDER_LAYERS[i].ordinal()] = new ArrayList<>(256);
        }

        if (MultithreadingUtils.MULTITHREADING_SUPPORTED) {
            backgroundTaskFutures = new Future[MultithreadingUtils.PARALLELISM];
            taskFutures = new Future[MultithreadingUtils.PARALLELISM];
            particlesStoreTasks = new ParticlesStoreTask[MultithreadingUtils.PARALLELISM];
            backgroundParticlesStoreTasks = new ParticlesStoreTask[MultithreadingUtils.PARALLELISM];
            for (int i = 0; i < particlesStoreTasks.length; i++) {
                particlesStoreTasks[i] = new ParticlesStoreTask(particlesByRenderLayer, RenderLayer.DEFAULT_ALPHA_BLENDED);
                particlesStoreTasks[i].init(vertexBuffers, materialBuffers);
                backgroundParticlesStoreTasks[i] = new ParticlesStoreTask(particlesByRenderLayer, RenderLayer.BACKGROUND_ALPHA_BLENDED);
                backgroundParticlesStoreTasks[i].init(vertexBuffers, materialBuffers);
            }
        }
    }

    private void checkBufferSizeAndClear() {
        boolean resized = false;

        for (int i = 0; i < RENDER_LAYERS.length; i++) {
            RenderLayer renderLayer = RENDER_LAYERS[i];
            List<Particle> particles = particlesByRenderLayer[renderLayer.ordinal()];
            int newDataSize = particles.size();

            ByteBuffer materialBuffer = materialBuffers[renderLayer.ordinal()];
            while (materialBuffer.capacity() < newDataSize << 5) {
                materialBuffer = BufferUtils.createByteBuffer(materialBuffer.capacity() << 1);
                materialBuffers[renderLayer.ordinal()] = materialBuffer;
                resized = true;
            }

            materialBuffer.clear();

            FloatBuffer vertexBuffer = vertexBuffers[renderLayer.ordinal()];
            while (vertexBuffer.capacity() < newDataSize << 4) {
                vertexBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity() << 1);
                vertexBuffers[renderLayer.ordinal()] = vertexBuffer;
                resized = true;
            }

            vertexBuffer.clear();
        }

        if (resized && MultithreadingUtils.MULTITHREADING_SUPPORTED) {
            for (int i = 0; i < particlesStoreTasks.length; i++) {
                particlesStoreTasks[i].init(vertexBuffers, materialBuffers);
                backgroundParticlesStoreTasks[i].init(vertexBuffers, materialBuffers);
            }
        }
    }

    public void putBackgroundParticlesToBuffers() {
        checkBufferSizeAndClear();

        int totalParticles = getParticlesCount();
        int multithreadedThreshold = 2048;
        int particlesByTask = 2048;

        multithreaded = MultithreadingUtils.MULTITHREADING_SUPPORTED && totalParticles >= multithreadedThreshold;
        taskCount = multithreaded ? (int) Math.ceil(Math.min(totalParticles / (float) particlesByTask, MultithreadingUtils.PARALLELISM)) : 1;

        int backgroundAlphaBufferIndex = 0;
        int backgroundAdditiveBufferIndex = 0;

        int backgroundAlphaParticlesPerTask = (int) Math.ceil(getParticles(RenderLayer.BACKGROUND_ALPHA_BLENDED).size() / (float) taskCount);
        int backgroundAlphaParticlesStartIndex = 0, backgroundAlphaParticlesEndIndex = backgroundAlphaParticlesPerTask;
        int backgroundAdditiveParticlesPerTask = (int) Math.ceil(getParticles(RenderLayer.BACKGROUND_ADDITIVE).size() / (float) taskCount);
        int backgroundAdditiveParticlesStartIndex = 0, backgroundAdditiveParticlesEndIndex = backgroundAdditiveParticlesPerTask;

        if (multithreaded) {
            for (int i = 0; i < taskCount; i++) {
                ParticlesStoreTask backgroundParticlesStoreTask = backgroundParticlesStoreTasks[i];

                backgroundParticlesStoreTask.update(backgroundAlphaBufferIndex, backgroundAdditiveBufferIndex, backgroundAlphaParticlesStartIndex, backgroundAlphaParticlesEndIndex,
                        backgroundAdditiveParticlesStartIndex, backgroundAdditiveParticlesEndIndex);

                backgroundTaskFutures[i] = SpriteRenderer.INSTANCE.addTask(backgroundParticlesStoreTask);

                backgroundAlphaBufferIndex += backgroundAlphaParticlesPerTask << 4;
                backgroundAdditiveBufferIndex += backgroundAdditiveParticlesPerTask << 4;

                backgroundAlphaParticlesStartIndex += backgroundAlphaParticlesPerTask;
                backgroundAlphaParticlesEndIndex = Math.min(backgroundAlphaParticlesEndIndex + backgroundAlphaParticlesPerTask, getParticles(RenderLayer.BACKGROUND_ALPHA_BLENDED).size());

                backgroundAdditiveParticlesStartIndex += backgroundAdditiveParticlesPerTask;
                backgroundAdditiveParticlesEndIndex = Math.min(backgroundAdditiveParticlesEndIndex + backgroundAdditiveParticlesPerTask, getParticles(RenderLayer.BACKGROUND_ADDITIVE).size());
            }
        } else {
            ParticlesStoreTask backgroundParticlesStoreTask = backgroundParticlesStoreTasks[0];
            backgroundParticlesStoreTask.update(backgroundAlphaBufferIndex, backgroundAdditiveBufferIndex, backgroundAlphaParticlesStartIndex, backgroundAlphaParticlesEndIndex,
                    backgroundAdditiveParticlesStartIndex, backgroundAdditiveParticlesEndIndex);
            backgroundParticlesStoreTask.run();
        }
    }

    public void putParticlesToBuffers() {
        int alphaBufferIndex = 0;
        int additiveBufferIndex = 0;

        int alphaParticlesPerTask = (int) Math.ceil(getParticles(RenderLayer.DEFAULT_ALPHA_BLENDED).size() / (float) taskCount);
        int alphaParticlesStartIndex = 0, alphaParticlesEndIndex = alphaParticlesPerTask;
        int additiveParticlesPerTask = (int) Math.ceil(getParticles(RenderLayer.DEFAULT_ADDITIVE).size() / (float) taskCount);
        int additiveParticlesStartIndex = 0, additiveParticlesEndIndex = additiveParticlesPerTask;

        if (multithreaded) {
            for (int i = 0; i < taskCount; i++) {
                ParticlesStoreTask particlesStoreTask = particlesStoreTasks[i];

                particlesStoreTask.update(alphaBufferIndex, additiveBufferIndex, alphaParticlesStartIndex, alphaParticlesEndIndex, additiveParticlesStartIndex, additiveParticlesEndIndex);

                taskFutures[i] = SpriteRenderer.INSTANCE.addTask(particlesStoreTasks[i]);

                alphaBufferIndex += alphaParticlesPerTask << 4;
                additiveBufferIndex += additiveParticlesPerTask << 4;

                alphaParticlesStartIndex += alphaParticlesPerTask;
                alphaParticlesEndIndex = Math.min(alphaParticlesEndIndex + alphaParticlesPerTask, getParticles(RenderLayer.DEFAULT_ALPHA_BLENDED).size());

                additiveParticlesStartIndex += additiveParticlesPerTask;
                additiveParticlesEndIndex = Math.min(additiveParticlesEndIndex + additiveParticlesPerTask, getParticles(RenderLayer.DEFAULT_ADDITIVE).size());
            }
        } else {
            ParticlesStoreTask particlesStoreTask = particlesStoreTasks[0];
            particlesStoreTask.update(alphaBufferIndex, additiveBufferIndex, alphaParticlesStartIndex, alphaParticlesEndIndex, additiveParticlesStartIndex, additiveParticlesEndIndex);
            particlesStoreTask.run();
        }
    }

    public void waitTasks(Future<?>[] taskFutures) {
        if (multithreaded) {
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
        waitTasks(backgroundTaskFutures);
        render(RenderLayer.BACKGROUND_ALPHA_BLENDED, RenderLayer.BACKGROUND_ADDITIVE);
    }

    public void render() {
        waitTasks(taskFutures);
        render(RenderLayer.DEFAULT_ALPHA_BLENDED, RenderLayer.DEFAULT_ADDITIVE);
    }

    private void render(RenderLayer alphaLayer, RenderLayer additiveLayer) {
        FloatBuffer vertexBuffer = vertexBuffers[alphaLayer.ordinal()];
        ByteBuffer materialBuffer = materialBuffers[alphaLayer.ordinal()];
        int count = particlesByRenderLayer[alphaLayer.ordinal()].size();

        if (count > 0) {
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            render(count, vertexBuffer, materialBuffer);
        }

        vertexBuffer = vertexBuffers[additiveLayer.ordinal()];
        materialBuffer = materialBuffers[additiveLayer.ordinal()];
        count = particlesByRenderLayer[additiveLayer.ordinal()].size();

        if (count > 0) {
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            render(count, vertexBuffer, materialBuffer);
        }
    }

    private void render(int count, FloatBuffer vertexBuffer, ByteBuffer materialBuffer) {
        SpriteRenderer.INSTANCE.render(count, vertexBuffer, materialBuffer);
    }

    public void addParticleToRenderLayer(Particle particle, RenderLayer renderLayer) {
        getParticles(renderLayer).add(particle);
    }

    public void removeParticleFromRenderLayer(Particle particle, RenderLayer renderLayer) {
        getParticles(renderLayer).remove(particle);
    }

    public void onExitToMainMenu() {
        for (int i = 0; i < particlesByRenderLayer.length; i++) {
            particlesByRenderLayer[i].clear();
        }
    }

    private List<Particle> getParticles(RenderLayer renderLayer) {
        return particlesByRenderLayer[renderLayer.ordinal()];
    }

    public int getParticlesCount() {
        return Core.get().getWorld().getParticleManager().getParticlesCount();
    }
}
