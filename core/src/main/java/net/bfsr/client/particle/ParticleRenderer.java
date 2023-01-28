package net.bfsr.client.particle;

import lombok.Getter;
import net.bfsr.client.model.TexturedQuad;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.ParticleInstancedShader;
import net.bfsr.core.Core;
import net.bfsr.math.Transformation;
import net.bfsr.settings.EnumOption;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParticleRenderer {
    @Getter
    private static ParticleRenderer instance;
    private static final int INSTANCE_DATA_LENGTH = 22;

    private final BaseShader defaultShader;
    @Getter
    private final ParticleInstancedShader particleShader = new ParticleInstancedShader();

    private final HashMap<String, List<Particle>> particlesHashMap = new HashMap<>();
    private final List<Particle> particles = new ArrayList<>();

    private final List<ParticleWreck> particlesWrecks = new ArrayList<>();

    private TexturedQuad quad;
    private ByteBuffer buffer = BufferUtils.createByteBuffer(INSTANCE_DATA_LENGTH * 4 * 4096);

    public ParticleRenderer(BaseShader baseShader) {
        this.defaultShader = baseShader;

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
                removeParticle(particle);
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

    private void removeParticle(ParticleWreck particle) {
        Core.getCore().getWorld().removeDynamicParticle(particle);
        particlesWrecks.remove(particle);
    }

    private void removeParticle(Particle particle) {
        particles.remove(particle);

        String renderType = particle.getRenderType().toString() + " " + particle.getPositionType().toString();

        List<Particle> particles = particlesHashMap.get(renderType);
        particles.remove(particle);
    }

    private void storeParticlesWrecks(List<ParticleWreck> particles, float interpolation) {
        int size = particles.size();
        checkBufferSize(particles.size() * INSTANCE_DATA_LENGTH * 4);

        for (int i = 0; i < size; i++) {
            ParticleWreck p = particles.get(i);
            if (p.getAABB().isIntersects(Core.getCore().getRenderer().getCamera().getBoundingBox()))
                storeParticle(p, interpolation);
        }
    }

    private int storeParticlesWrecksEffects(List<ParticleWreck> particles, float interpolation) {
        int size = particles.size();
        checkBufferSize(particles.size() * INSTANCE_DATA_LENGTH * 4 * 2);

        int count = 0;
        for (int i = 0; i < size; i++) {
            ParticleWreck particleWreck = particles.get(i);
            if (particleWreck.getColorFire() != null && particleWreck.getColorFire().w > 0) {
                storeMatrix(Transformation.getModelMatrix(particleWreck, interpolation));
                storeColor(particleWreck.getColorFire());
                storeTextureHandle(particleWreck.getTextureFire().getTextureHandle());
                count++;
            }

            if (particleWreck.getColorLight() != null && particleWreck.getColorLight().w > 0) {
                storeMatrix(Transformation.getModelMatrix(particleWreck, interpolation));
                storeColor(particleWreck.getColorLight());
                storeTextureHandle(particleWreck.getTextureLight().getTextureHandle());
                count++;
            }
        }

        return count;
    }

    private void storeParticles(List<Particle> particles, float interpolation) {
        checkBufferSize(particles.size() * INSTANCE_DATA_LENGTH * 4);

        for (int i = 0; i < particles.size(); i++) {
            storeParticle(particles.get(i), interpolation);
        }
    }

    private void checkBufferSize(int newDataSize) {
        while (buffer.capacity() - buffer.position() < newDataSize) {
            ByteBuffer newBuffer = BufferUtils.createByteBuffer(buffer.capacity() << 1);
            if (buffer.position() > 0) newBuffer.put(buffer.flip());
            buffer = newBuffer;
        }
    }

    private void storeParticle(Particle particle, float interpolation) {
        storeMatrix(Transformation.getModelMatrix(particle, interpolation));
        storeParticleData(particle);
    }

    private void render(int count) {
        quad.updateVertexBuffer(2, buffer.flip(), INSTANCE_DATA_LENGTH);
        GL30C.glBindVertexArray(quad.getVaoId());
        GL31C.glDrawArraysInstanced(GL11C.GL_QUADS, 0, 4, count);
        Core.getCore().getRenderer().increaseDrawCalls();
    }

    private void storeParticleData(Particle particle) {
        storeColor(particle.getColor());
        storeTextureHandle(particle.getTexture().getTextureHandle());
    }

    private void storeMatrix(FloatBuffer matrix) {
        for (int j = 0; j < 16; j++) {
            buffer.putFloat(matrix.get(j));
        }
    }

    private void storeColor(Vector4f color) {
        buffer.putFloat(color.x);
        buffer.putFloat(color.y);
        buffer.putFloat(color.z);
        buffer.putFloat(color.w);
    }

    private void storeTextureHandle(long textureHandle) {
        buffer.putLong(textureHandle);
    }

    public void render(EnumParticlePositionType positionType, float interpolation) {
        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        particleShader.enable();
        buffer.clear();
        int count = 0;

        //render wrecks
        if (positionType == EnumParticlePositionType.Default) {
            storeParticlesWrecks(particlesWrecks, interpolation);
            count += particlesWrecks.size();
        }

        String renderType = EnumParticleRenderType.AlphaBlended + " " + positionType.toString();
        List<Particle> particles = particlesHashMap.get(renderType);
        if (particles != null) {
            storeParticles(particles, interpolation);
            count += particles.size();
        }

        render(count);

        if (EnumOption.SHOW_DEBUG_BOXES.getBoolean()) {
            Core.getCore().getRenderer().getCamera().setupOpenGLMatrix();

            GL20.glUseProgram(0);
            for (int i = 0; i < particlesWrecks.size(); i++) {
                ParticleWreck particle = particlesWrecks.get(i);
                particle.renderDebug();
            }
            defaultShader.enable();
        }

        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        buffer.clear();
        count = 0;

        if (positionType == EnumParticlePositionType.Default) {
            count += storeParticlesWrecksEffects(particlesWrecks, interpolation);
        }

        renderType = EnumParticleRenderType.Additive + " " + positionType;
        particles = particlesHashMap.get(renderType);
        if (particles != null) {
            storeParticles(particles, interpolation);
            count += particles.size();
        }

        render(count);

        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    void addParticle(ParticleWreck particle) {
        particlesWrecks.add(particle);
    }

    void addParticle(Particle particle) {
        EnumParticlePositionType positionType = particle.getPositionType();
        EnumParticleRenderType renderType = particle.getRenderType();

        String fullRenderType = renderType.toString() + " " + positionType.toString();
        List<Particle> particles = particlesHashMap.computeIfAbsent(fullRenderType, s -> new ArrayList<>());

        particles.add(particle);
        this.particles.add(particle);
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public List<ParticleWreck> getParticlesWrecks() {
        return particlesWrecks;
    }
}
