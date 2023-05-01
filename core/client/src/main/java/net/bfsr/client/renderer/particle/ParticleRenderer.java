package net.bfsr.client.renderer.particle;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.core.Core;
import net.bfsr.client.renderer.SpriteRenderer;
import net.bfsr.render.RenderLayer;
import net.bfsr.util.MultithreadingUtils;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.lwjgl.opengl.GL11C.*;

@Log4j2
public class ParticleRenderer {
    public static final RenderLayer[] RENDER_LAYERS = RenderLayer.values();
    private static final int START_PARTICLE_COUNT = 8192;

    private SpriteRenderer spriteRenderer = new SpriteRenderer();
    private final List<ParticleRender>[] particlesByRenderLayer = new List[4];
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
            materialBuffers[RENDER_LAYERS[i].ordinal()] = BufferUtils.createByteBuffer(START_PARTICLE_COUNT * SpriteRenderer.MATERIAL_DATA_SIZE_IN_BYTES);
            vertexBuffers[RENDER_LAYERS[i].ordinal()] = BufferUtils.createFloatBuffer(START_PARTICLE_COUNT * SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
            particlesByRenderLayer[RENDER_LAYERS[i].ordinal()] = new ArrayList<>(256);
        }

        particlesStoreTasks = new ParticlesStoreTask[MultithreadingUtils.PARALLELISM];
        backgroundParticlesStoreTasks = new ParticlesStoreTask[MultithreadingUtils.PARALLELISM];
        for (int i = 0; i < particlesStoreTasks.length; i++) {
            particlesStoreTasks[i] = new ParticlesStoreTask(particlesByRenderLayer, RenderLayer.DEFAULT_ALPHA_BLENDED);
            backgroundParticlesStoreTasks[i] = new ParticlesStoreTask(particlesByRenderLayer, RenderLayer.BACKGROUND_ALPHA_BLENDED);
        }

        if (MultithreadingUtils.MULTITHREADING_SUPPORTED) {
            backgroundTaskFutures = new Future[MultithreadingUtils.PARALLELISM];
            taskFutures = new Future[MultithreadingUtils.PARALLELISM];
        }
    }

    public void init(SpriteRenderer spriteRenderer) {
        this.spriteRenderer = spriteRenderer;

        for (int i = 0; i < particlesStoreTasks.length; i++) {
            particlesStoreTasks[i].init(spriteRenderer, vertexBuffers, materialBuffers);
            backgroundParticlesStoreTasks[i].init(spriteRenderer, vertexBuffers, materialBuffers);
        }
    }

    private void checkBufferSizeAndClear() {
        boolean resized = false;

        for (int i = 0; i < RENDER_LAYERS.length; i++) {
            RenderLayer renderLayer = RENDER_LAYERS[i];
            List<ParticleRender> renders = particlesByRenderLayer[renderLayer.ordinal()];
            int newDataSize = renders.size();

            ByteBuffer materialBuffer = materialBuffers[renderLayer.ordinal()];
            while (materialBuffer.capacity() < newDataSize * SpriteRenderer.MATERIAL_DATA_SIZE_IN_BYTES) {
                materialBuffer = BufferUtils.createByteBuffer(materialBuffer.capacity() << 1);
                materialBuffers[renderLayer.ordinal()] = materialBuffer;
                resized = true;
            }

            materialBuffer.clear();

            FloatBuffer vertexBuffer = vertexBuffers[renderLayer.ordinal()];
            while (vertexBuffer.capacity() < newDataSize * SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES) {
                vertexBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity() << 1);
                vertexBuffers[renderLayer.ordinal()] = vertexBuffer;
                resized = true;
            }

            vertexBuffer.clear();
        }

        if (resized && MultithreadingUtils.MULTITHREADING_SUPPORTED) {
            for (int i = 0; i < particlesStoreTasks.length; i++) {
                particlesStoreTasks[i].initRunnable(vertexBuffers, materialBuffers);
                backgroundParticlesStoreTasks[i].initRunnable(vertexBuffers, materialBuffers);
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

                backgroundParticlesStoreTask.update(backgroundAlphaBufferIndex, backgroundAdditiveBufferIndex, backgroundAlphaParticlesStartIndex,
                        backgroundAlphaParticlesEndIndex, backgroundAdditiveParticlesStartIndex, backgroundAdditiveParticlesEndIndex);

                backgroundTaskFutures[i] = spriteRenderer.addTask(backgroundParticlesStoreTask);

                backgroundAlphaBufferIndex += backgroundAlphaParticlesPerTask * SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES;
                backgroundAdditiveBufferIndex += backgroundAdditiveParticlesPerTask * SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES;

                backgroundAlphaParticlesStartIndex += backgroundAlphaParticlesPerTask;
                backgroundAlphaParticlesEndIndex = Math.min(backgroundAlphaParticlesEndIndex + backgroundAlphaParticlesPerTask,
                        getParticles(RenderLayer.BACKGROUND_ALPHA_BLENDED).size());

                backgroundAdditiveParticlesStartIndex += backgroundAdditiveParticlesPerTask;
                backgroundAdditiveParticlesEndIndex = Math.min(backgroundAdditiveParticlesEndIndex + backgroundAdditiveParticlesPerTask,
                        getParticles(RenderLayer.BACKGROUND_ADDITIVE).size());
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

                particlesStoreTask.update(alphaBufferIndex, additiveBufferIndex, alphaParticlesStartIndex, alphaParticlesEndIndex, additiveParticlesStartIndex,
                        additiveParticlesEndIndex);

                taskFutures[i] = spriteRenderer.addTask(particlesStoreTasks[i]);

                alphaBufferIndex += alphaParticlesPerTask * SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES;
                additiveBufferIndex += additiveParticlesPerTask * SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES;

                alphaParticlesStartIndex += alphaParticlesPerTask;
                alphaParticlesEndIndex = Math.min(alphaParticlesEndIndex + alphaParticlesPerTask, getParticles(RenderLayer.DEFAULT_ALPHA_BLENDED).size());

                additiveParticlesStartIndex += additiveParticlesPerTask;
                additiveParticlesEndIndex = Math.min(additiveParticlesEndIndex + additiveParticlesPerTask, getParticles(RenderLayer.DEFAULT_ADDITIVE).size());
            }
        } else {
            ParticlesStoreTask particlesStoreTask = particlesStoreTasks[0];
            particlesStoreTask.update(alphaBufferIndex, additiveBufferIndex, alphaParticlesStartIndex, alphaParticlesEndIndex, additiveParticlesStartIndex,
                    additiveParticlesEndIndex);
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

    public void update() {
        for (int i = 0; i < particlesByRenderLayer.length; i++) {
            List<ParticleRender> renders = particlesByRenderLayer[i];
            for (int i1 = 0; i1 < renders.size(); i1++) {
                ParticleRender render = renders.get(i1);
                render.update();
                if (render.isDead()) {
                    renders.remove(i1--);
                }
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
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            spriteRenderer.render(count, vertexBuffer, materialBuffer);
        }

        vertexBuffer = vertexBuffers[additiveLayer.ordinal()];
        materialBuffer = materialBuffers[additiveLayer.ordinal()];
        count = particlesByRenderLayer[additiveLayer.ordinal()].size();

        if (count > 0) {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            spriteRenderer.render(count, vertexBuffer, materialBuffer);
        }
    }

    public void addParticleToRenderLayer(ParticleRender render, RenderLayer renderLayer) {
        getParticles(renderLayer).add(render);
    }

    public void onExitToMainMenu() {
        for (int i = 0; i < particlesByRenderLayer.length; i++) {
            particlesByRenderLayer[i].clear();
        }
    }

    private List<ParticleRender> getParticles(RenderLayer renderLayer) {
        return particlesByRenderLayer[renderLayer.ordinal()];
    }

    public int getParticlesCount() {
        return Core.get().getWorld().getParticleManager().getParticlesCount();
    }
}