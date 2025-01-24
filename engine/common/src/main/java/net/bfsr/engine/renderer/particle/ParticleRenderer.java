package net.bfsr.engine.renderer.particle;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.culling.AbstractGPUFrustumCullingSystem;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.util.MultithreadingUtils;
import net.bfsr.engine.util.ObjectPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Log4j2
public class ParticleRenderer {
    private static final int START_PARTICLE_COUNT = 8192;
    private static final int MULTITHREADED_THRESHOLD = 20000;

    private AbstractRenderer renderer;
    private AbstractSpriteRenderer spriteRenderer;
    private AbstractGPUFrustumCullingSystem cullingSystem;
    private AbstractBuffersHolder[] buffersHolderArray;

    private final ObjectPool<ParticleRender>[] renderPool = new ObjectPool[RenderLayer.VALUES.length];
    private final List<ParticleRender>[] particlesByRenderLayer = new List[RenderLayer.VALUES.length];
    private final ParticlesStoreRunnable[] particlesStoreRunnables;
    private final ParticlesStoreRunnable[] backgroundParticlesStoreRunnables;
    private Future<?>[] taskFutures;
    private Future<?>[] backgroundTaskFutures;
    @Getter
    private int taskCount;
    private boolean multithreaded;

    public ParticleRenderer() {
        RenderLayer[] renderLayers = RenderLayer.VALUES;
        for (int i = 0; i < renderLayers.length; i++) {
            particlesByRenderLayer[renderLayers[i].ordinal()] = new ArrayList<>(256);
        }

        particlesStoreRunnables = new ParticlesStoreRunnable[MultithreadingUtils.PARALLELISM];
        backgroundParticlesStoreRunnables = new ParticlesStoreRunnable[MultithreadingUtils.PARALLELISM];
        for (int i = 0; i < particlesStoreRunnables.length; i++) {
            particlesStoreRunnables[i] = new ParticlesStoreRunnable(particlesByRenderLayer, RenderLayer.DEFAULT_ALPHA_BLENDED);
            backgroundParticlesStoreRunnables[i] = new ParticlesStoreRunnable(particlesByRenderLayer,
                    RenderLayer.BACKGROUND_ALPHA_BLENDED);
        }

        if (MultithreadingUtils.MULTITHREADING_SUPPORTED) {
            backgroundTaskFutures = new Future[MultithreadingUtils.PARALLELISM];
            taskFutures = new Future[MultithreadingUtils.PARALLELISM];
        }
    }

    public void init(AbstractRenderer renderer) {
        this.renderer = renderer;
        spriteRenderer = renderer.getSpriteRenderer();
        cullingSystem = renderer.getCullingSystem();
        buffersHolderArray = spriteRenderer.createBuffersHolderArray(RenderLayer.VALUES.length);

        RenderLayer[] renderLayers = RenderLayer.VALUES;
        for (int i = 0; i < renderLayers.length; i++) {
            buffersHolderArray[renderLayers[i].ordinal()] = spriteRenderer.createBuffersHolder(START_PARTICLE_COUNT, true);
            renderPool[renderLayers[i].ordinal()] = new ObjectPool<>(() -> new ParticleRender(renderer));
        }

        for (int i = 0; i < particlesStoreRunnables.length; i++) {
            particlesStoreRunnables[i].init();
            backgroundParticlesStoreRunnables[i].init();
        }
    }

    public void putBackgroundParticlesToBuffers(int totalParticles) {
        checkBufferSize();

        spriteRenderer.updateBuffers(buffersHolderArray);
        spriteRenderer.waitForLockedRange(buffersHolderArray);

        multithreaded = MultithreadingUtils.MULTITHREADING_SUPPORTED && totalParticles >= MULTITHREADED_THRESHOLD;
        taskCount = multithreaded ?
                (int) Math.ceil(Math.min(totalParticles / (float) MULTITHREADED_THRESHOLD, MultithreadingUtils.PARALLELISM)) : 1;

        putToBuffers(RenderLayer.BACKGROUND_ALPHA_BLENDED, RenderLayer.BACKGROUND_ADDITIVE, backgroundTaskFutures,
                backgroundParticlesStoreRunnables);
    }

    private void checkBufferSize() {
        for (int i = 0; i < RenderLayer.VALUES.length; i++) {
            buffersHolderArray[i].checkBuffersSize(particlesByRenderLayer[i].size());
        }
    }

    public void putParticlesToBuffers() {
        putToBuffers(RenderLayer.DEFAULT_ALPHA_BLENDED, RenderLayer.DEFAULT_ADDITIVE, taskFutures, particlesStoreRunnables);
    }

    private void putToBuffers(RenderLayer alphaLayer, RenderLayer additiveLayer, Future<?>[] taskFutures,
                              ParticlesStoreRunnable[] particlesStoreRunnables) {
        int alphaParticlesPerTask = (int) Math.ceil(getParticles(alphaLayer).size() / (float) taskCount);
        int alphaParticlesStartIndex = 0, alphaParticlesEndIndex = alphaParticlesPerTask;
        int additiveParticlesPerTask = (int) Math.ceil(getParticles(additiveLayer).size() / (float) taskCount);
        int additiveParticlesStartIndex = 0, additiveParticlesEndIndex = additiveParticlesPerTask;

        if (multithreaded) {
            for (int i = 0; i < taskCount; i++) {
                ParticlesStoreRunnable particlesStoreRunnable = particlesStoreRunnables[i];

                particlesStoreRunnable.update(alphaParticlesStartIndex, alphaParticlesEndIndex, additiveParticlesStartIndex,
                        additiveParticlesEndIndex);

                taskFutures[i] = spriteRenderer.addTask(particlesStoreRunnables[i]);

                alphaParticlesStartIndex += alphaParticlesPerTask;
                alphaParticlesEndIndex = Math.min(alphaParticlesEndIndex + alphaParticlesPerTask,
                        getParticles(alphaLayer).size());

                additiveParticlesStartIndex += additiveParticlesPerTask;
                additiveParticlesEndIndex = Math.min(additiveParticlesEndIndex + additiveParticlesPerTask,
                        getParticles(additiveLayer).size());
            }
        } else {
            ParticlesStoreRunnable particlesStoreRunnable = particlesStoreRunnables[0];
            particlesStoreRunnable.update(alphaParticlesStartIndex, alphaParticlesEndIndex, additiveParticlesStartIndex,
                    additiveParticlesEndIndex);
            particlesStoreRunnable.run();
        }
    }

    private void waitTasks(Future<?>[] taskFutures) {
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

    public void waitBackgroundTasks() {
        waitTasks(backgroundTaskFutures);
    }

    public void renderBackground() {
        render(RenderLayer.BACKGROUND_ALPHA_BLENDED, RenderLayer.BACKGROUND_ADDITIVE);
    }

    public void waitTasks() {
        waitTasks(taskFutures);
    }

    public void render() {
        render(RenderLayer.DEFAULT_ALPHA_BLENDED, RenderLayer.DEFAULT_ADDITIVE);
    }

    private void render(RenderLayer alphaLayer, RenderLayer additiveLayer) {
        render(alphaLayer.ordinal(), GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        render(additiveLayer.ordinal(), GL.GL_SRC_ALPHA, GL.GL_ONE);
    }

    private void render(int bufferIndex, int sFactor, int dFactor) {
        int count = particlesByRenderLayer[bufferIndex].size();
        if (count == 0) {
            return;
        }

        renderer.glBlendFunc(sFactor, dFactor);
        AbstractBuffersHolder buffersHolder = buffersHolderArray[bufferIndex];
        if (renderer.isParticlesGPUFrustumCulling()) {
            cullingSystem.renderFrustumCulled(count, buffersHolder);
        } else {
            spriteRenderer.render(count, buffersHolder);
        }
    }

    public void setPersistentMappedBuffers(boolean value) {
        if (value) {
            for (int i = 0; i < buffersHolderArray.length; i++) {
                buffersHolderArray[i].enablePersistentMapping();
            }
        } else {
            for (int i = 0; i < buffersHolderArray.length; i++) {
                buffersHolderArray[i].disablePersistentMapping();
            }
        }
    }

    public void onParticlesGPUOcclusionCullingChangeValue() {
        for (int i = 0; i < buffersHolderArray.length; i++) {
            buffersHolderArray[i].fillCommandBufferWithDefaultValues();
        }
    }

    public void addParticleToRenderLayer(ParticleRender render, RenderLayer renderLayer) {
        getParticles(renderLayer).add(render);
    }

    private List<ParticleRender> getParticles(RenderLayer renderLayer) {
        return particlesByRenderLayer[renderLayer.ordinal()];
    }

    public AbstractBuffersHolder getBuffersHolder(RenderLayer renderLayer) {
        return buffersHolderArray[renderLayer.ordinal()];
    }

    public ParticleRender newRender(RenderLayer renderLayer) {
        return renderPool[renderLayer.ordinal()].get();
    }

    public void remove(RenderLayer renderLayer, ParticleRender render) {
        renderPool[renderLayer.ordinal()].returnBack(render);
    }

    public void clear() {
        removeAllRenders();

        for (int i = 0; i < buffersHolderArray.length; i++) {
            buffersHolderArray[i].clear();
        }
    }

    public void removeAllRenders() {
        for (int i = 0; i < particlesByRenderLayer.length; i++) {
            particlesByRenderLayer[i].clear();
        }
    }
}