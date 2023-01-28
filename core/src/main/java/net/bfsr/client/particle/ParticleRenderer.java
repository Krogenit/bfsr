package net.bfsr.client.particle;

import lombok.Getter;
import net.bfsr.client.camera.Camera;
import net.bfsr.client.model.TexturedQuad;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.render.texture.Texture;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.ParticleInstancedShader;
import net.bfsr.core.Core;
import net.bfsr.math.Transformation;
import net.bfsr.settings.EnumOption;
import net.bfsr.world.WorldClient;
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

    private final Core core = Core.getCore();
    private final WorldClient world;
    private final Camera cam;

    private final BaseShader defaultShader;
    private final ParticleInstancedShader particleShader;

    private final HashMap<String, List<Particle>> particlesHashMap = new HashMap<>();
    private final List<Particle> particles = new ArrayList<>();

    private final HashMap<Texture, List<ParticleWreck>> particlesWrecksHashMap = new HashMap<>();
    private final List<ParticleWreck> particlesWrecks = new ArrayList<>();

    private final TexturedQuad quad;
    private ByteBuffer buffer = BufferUtils.createByteBuffer(INSTANCE_DATA_LENGTH * 1000 * 4);

    public ParticleRenderer(WorldClient w) {
        this.world = w;
        this.cam = core.getRenderer().getCamera();
        this.defaultShader = core.getRenderer().getShader();
        this.particleShader = new ParticleInstancedShader();
        this.particleShader.load();
        this.particleShader.init();
        quad = TexturedQuad.createParticleCenteredQuad();
        quad.addInstancedAttribute(2, 1, 4, INSTANCE_DATA_LENGTH, 0);
        quad.addInstancedAttribute(2, 2, 4, INSTANCE_DATA_LENGTH, 4);
        quad.addInstancedAttribute(2, 3, 4, INSTANCE_DATA_LENGTH, 8);
        quad.addInstancedAttribute(2, 4, 4, INSTANCE_DATA_LENGTH, 12);
        quad.addInstancedAttribute(2, 5, 4, INSTANCE_DATA_LENGTH, 16);
        quad.addInstancedAttribute(2, 6, 2, INSTANCE_DATA_LENGTH, 20);
        instance = this;
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
        world.removeDynamicParticle(particle);
        particlesWrecks.remove(particle);

        List<ParticleWreck> particlesByTexture = particlesWrecksHashMap.get(particle.getTexture());
        particlesByTexture.remove(particle);
    }

    private void removeParticle(Particle particle) {
        particles.remove(particle);

        String renderType = particle.getRenderType().toString() + " " + particle.getPositionType().toString();

        List<Particle> particles = particlesHashMap.get(renderType);
        particles.remove(particle);
    }

    private void renderParticlesWrecks(List<ParticleWreck> particles) {
        if (particles.size() > 0) {
            Particle p = particles.get(0);
            OpenGLHelper.alphaGreater(p.getGreater());
        }

        int size = particles.size();
        for (int i = 0; i < size; i++) {
            ParticleWreck p = particles.get(i);
            if (p.getAABB().isIntersects(cam.getBoundingBox()))
                p.render(defaultShader);
        }

        if (EnumOption.SHOW_DEBUG_BOXES.getBoolean()) {
            core.getRenderer().getCamera().setupOpenGLMatrix();

            GL20.glUseProgram(0);
            size = particles.size();
            for (int i = 0; i < size; i++) {
                ParticleWreck particle = particles.get(i);
                particle.renderDebug();
            }
            defaultShader.enable();
        }
    }

    private void renderParticlesWrecksEffects(List<ParticleWreck> paritcles) {
        int size = paritcles.size();
        for (int i = 0; i < size; i++) {
            ParticleWreck paritcle = paritcles.get(i);
            paritcle.renderEffects(defaultShader);
        }
    }

    private void renderParticles(List<Particle> particles, float interpolation) {
        if (particles.size() > 0) {
            Particle p = particles.get(0);
            OpenGLHelper.alphaGreater(p.getGreater());
        }

        int size = particles.size() * INSTANCE_DATA_LENGTH * 4;

        buffer.clear();

        while (buffer.capacity() < size) {
            buffer = BufferUtils.createByteBuffer(buffer.capacity() << 1);
        }

        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            FloatBuffer matrix = Transformation.getModelMatrix(particle, interpolation);
            for (int j = 0; j < 16; j++) {
                buffer.putFloat(matrix.get(j));
            }
            storeParticleData(particle);
        }

        quad.updateVertexBuffer(2, buffer.flip(), INSTANCE_DATA_LENGTH);
        GL30C.glBindVertexArray(quad.getVaoId());
        GL31C.glDrawArraysInstanced(GL11C.GL_QUADS, 0, 4, particles.size());
        core.getRenderer().increaseDrawCalls();
    }

    private void storeParticleData(Particle particle) {
        Vector4f color = particle.getColor();
        buffer.putFloat(color.x);
        buffer.putFloat(color.y);
        buffer.putFloat(color.z);
        buffer.putFloat(color.w);
        buffer.putLong(particle.getTexture().getTextureHandle());
    }

    public void render(EnumParticlePositionType positionType, float interpolation) {
        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        //render wrecks
        if (positionType == EnumParticlePositionType.Default) {
            defaultShader.enable();
            for (Texture texture : particlesWrecksHashMap.keySet()) {
                renderParticlesWrecks(particlesWrecksHashMap.get(texture));
            }
        }

        particleShader.enable();

        String renderType = EnumParticleRenderType.AlphaBlended + " " + positionType.toString();
        List<Particle> particles = particlesHashMap.get(renderType);
        if (particles != null) {
            renderParticles(particles, interpolation);
        }

        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        //render wrecks effects
        defaultShader.enable();

        if (positionType == EnumParticlePositionType.Default) {
            OpenGLHelper.alphaGreater(0.001f);
            for (Texture texture : particlesWrecksHashMap.keySet()) {
                renderParticlesWrecksEffects(particlesWrecksHashMap.get(texture));
            }
        }

        particleShader.enable();

        renderType = EnumParticleRenderType.Additive + " " + positionType;
        particles = particlesHashMap.get(renderType);
        if (particles != null) {
            renderParticles(particles, interpolation);
        }

        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    void addParticle(ParticleWreck particle) {
        Texture texture = particle.getTexture();
        List<ParticleWreck> particles = particlesWrecksHashMap.computeIfAbsent(texture, texture1 -> new ArrayList<>(32));
        particles.add(particle);
        particlesWrecksHashMap.put(texture, particles);
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
