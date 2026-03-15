package net.bfsr.engine.renderer.particle;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.constant.BlendFactor;
import net.bfsr.engine.renderer.culling.AbstractGPUFrustumCullingSystem;
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

    private final ObjectPool<ParticleRender>[] renderPool = new ObjectPool[ParticleType.VALUES.length];
    private final List<ParticleRender>[] particlesByType = new List[ParticleType.VALUES.length];
    private final ParticlesStoreRunnable[] particlesStoreRunnables;
    private Future<?>[] taskFutures;
    @Getter
    private int taskCount;
    private boolean multithreaded;

    public ParticleRenderer() {
        ParticleType[] particleTypes = ParticleType.VALUES;
        for (int i = 0; i < particleTypes.length; i++) {
            particlesByType[particleTypes[i].ordinal()] = new ArrayList<>(256);
            renderPool[particleTypes[i].ordinal()] = new ObjectPool<>(ParticleRender::new);
        }

        particlesStoreRunnables = new ParticlesStoreRunnable[MultithreadingUtils.PARALLELISM];
        for (int i = 0; i < particlesStoreRunnables.length; i++) {
            particlesStoreRunnables[i] = new ParticlesStoreRunnable(particlesByType);
        }

        if (MultithreadingUtils.MULTITHREADING_SUPPORTED) {
            taskFutures = new Future[MultithreadingUtils.PARALLELISM];
        }
    }

    public void init(AbstractRenderer renderer) {
        this.renderer = renderer;
        spriteRenderer = renderer.getSpriteRenderer();
        cullingSystem = renderer.getCullingSystem();
        buffersHolderArray = spriteRenderer.createBuffersHolderArray(ParticleType.VALUES.length);

        ParticleType[] particleTypes = ParticleType.VALUES;
        for (int i = 0; i < particleTypes.length; i++) {
            buffersHolderArray[particleTypes[i].ordinal()] = spriteRenderer.createBuffersHolder(START_PARTICLE_COUNT, true);
        }

        for (int i = 0; i < particlesStoreRunnables.length; i++) {
            particlesStoreRunnables[i].init();
        }
    }

    public void putParticlesToBuffers(int totalParticles) {
        checkBufferSize();

        spriteRenderer.updateBuffers(buffersHolderArray);
        spriteRenderer.waitForLockedRange(buffersHolderArray);

        multithreaded = MultithreadingUtils.MULTITHREADING_SUPPORTED && totalParticles >= MULTITHREADED_THRESHOLD;
        taskCount = multithreaded ?
                (int) Math.ceil(Math.min(totalParticles / (float) MULTITHREADED_THRESHOLD, MultithreadingUtils.PARALLELISM)) : 1;

        putToBuffers(ParticleType.ALPHA_BLENDED, ParticleType.ADDITIVE, taskFutures, particlesStoreRunnables);
    }

    private void checkBufferSize() {
        for (int i = 0; i < ParticleType.VALUES.length; i++) {
            buffersHolderArray[i].checkBuffersSize(particlesByType[i].size());
        }
    }

    private void putToBuffers(ParticleType alphaLayer, ParticleType additiveLayer, Future<?>[] taskFutures,
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
        for (int i = 0; i < particlesByType.length; i++) {
            List<ParticleRender> renders = particlesByType[i];
            for (int i1 = 0; i1 < renders.size(); i1++) {
                ParticleRender render = renders.get(i1);
                render.update();
                if (render.isDead()) {
                    renders.remove(i1--);
                }
            }
        }
    }

    public void waitTasks() {
        waitTasks(taskFutures);
    }

    public void render() {
        render(ParticleType.ALPHA_BLENDED.ordinal(), BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
        render(ParticleType.ADDITIVE.ordinal(), BlendFactor.SRC_ALPHA, BlendFactor.ONE);
    }

    private void render(int bufferIndex, BlendFactor sFactor, BlendFactor dFactor) {
        int count = particlesByType[bufferIndex].size();
        if (count == 0) {
            return;
        }

        renderer.blendFunc(sFactor, dFactor);
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

    public void addParticle(ParticleRender render, ParticleType particleType) {
        getParticles(particleType).add(render);
    }

    private List<ParticleRender> getParticles(ParticleType particleType) {
        return particlesByType[particleType.ordinal()];
    }

    public AbstractBuffersHolder getBuffersHolder(ParticleType particleType) {
        return buffersHolderArray[particleType.ordinal()];
    }

    public ParticleRender newRender(ParticleType particleType) {
        return renderPool[particleType.ordinal()].get();
    }

    public void remove(ParticleType particleType, ParticleRender render) {
        renderPool[particleType.ordinal()].returnBack(render);
    }

    public void clear() {
        removeAllRenders();

        for (int i = 0; i < buffersHolderArray.length; i++) {
            buffersHolderArray[i].clear();
        }
    }

    public void removeAllRenders() {
        for (int i = 0; i < particlesByType.length; i++) {
            particlesByType[i].clear();
        }
    }
}