package net.bfsr.engine.renderer.particle;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.Engine;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.util.MultithreadingUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Log4j2
public class ParticleRenderer {
    private static final int START_PARTICLE_COUNT = 8192;
    private static final int MULTITHREADED_THRESHOLD = 16384;
    private static final int PARTICLES_BY_TASK = 16384;

    private final AbstractRenderer renderer = Engine.renderer;
    private final AbstractSpriteRenderer spriteRenderer = renderer.spriteRenderer;
    private final List<ParticleRender>[] particlesByRenderLayer = new List[4];
    private final AbstractBuffersHolder[] buffersHolderArray = spriteRenderer.createBuffersHolderArray(4);
    private final ParticlesStoreRunnable[] particlesStoreRunnables;
    private final ParticlesStoreRunnable[] backgroundParticlesStoreRunnables;
    private Future<?>[] taskFutures;
    private Future<?>[] backgroundTaskFutures;
    @Getter
    private int taskCount;
    private boolean multithreaded;
    private final Profiler profiler;

    public ParticleRenderer(Profiler profiler) {
        this.profiler = profiler;
        RenderLayer[] renderLayers = RenderLayer.VALUES;
        for (int i = 0; i < renderLayers.length; i++) {
            buffersHolderArray[renderLayers[i].ordinal()] = spriteRenderer.createBuffersHolder(START_PARTICLE_COUNT, true);
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

    public void init() {
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
                (int) Math.ceil(Math.min(totalParticles / (float) PARTICLES_BY_TASK, MultithreadingUtils.PARALLELISM)) : 1;

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

    public void renderBackground() {
        profiler.start("waitTasks");
        waitTasks(backgroundTaskFutures);
        profiler.end();
        render(RenderLayer.BACKGROUND_ALPHA_BLENDED, RenderLayer.BACKGROUND_ADDITIVE);
    }

    public void render() {
        profiler.start("waitTasks");
        waitTasks(taskFutures);
        profiler.end();
        render(RenderLayer.DEFAULT_ALPHA_BLENDED, RenderLayer.DEFAULT_ADDITIVE);
    }

    private void render(RenderLayer alphaLayer, RenderLayer additiveLayer) {
        int count = particlesByRenderLayer[alphaLayer.ordinal()].size();
        if (count > 0) {
            renderer.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            spriteRenderer.render(count, buffersHolderArray[alphaLayer.ordinal()]);
        }

        count = particlesByRenderLayer[additiveLayer.ordinal()].size();
        if (count > 0) {
            renderer.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
            spriteRenderer.render(count, buffersHolderArray[additiveLayer.ordinal()]);
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

    public void clear() {
        for (int i = 0; i < particlesByRenderLayer.length; i++) {
            particlesByRenderLayer[i].clear();
        }
    }
}