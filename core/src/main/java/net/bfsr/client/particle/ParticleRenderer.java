package net.bfsr.client.particle;

import lombok.Getter;
import net.bfsr.client.camera.Camera;
import net.bfsr.client.model.MeshLoader;
import net.bfsr.client.model.TexturedQuad;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.render.texture.Texture;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.ParticleInstancedShader;
import net.bfsr.core.Core;
import net.bfsr.math.Transformation;
import net.bfsr.settings.EnumOption;
import net.bfsr.world.WorldClient;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParticleRenderer {
    @Getter
    private static ParticleRenderer instance;
    private static final int INSTANCE_DATA_LENGTH = 25;

    private final Core core = Core.getCore();
    private final WorldClient world;
    private final Camera cam;

    private final BaseShader defaultShader;
    private final ParticleInstancedShader particleShader;

    private final HashMap<String, HashMap<Texture, List<Particle>>> particlesHashMap = new HashMap<>();
    private final List<Particle> particles = new ArrayList<>();

    private final HashMap<Texture, List<ParticleWreck>> particlesWrecksHashMap = new HashMap<>();
    private final List<ParticleWreck> particlesWrecks = new ArrayList<>();

    private final TexturedQuad quad;
    private FloatBuffer buffer = BufferUtils.createFloatBuffer(INSTANCE_DATA_LENGTH * 1000);

    public ParticleRenderer(WorldClient w) {
        this.world = w;
        this.cam = core.getRenderer().getCamera();
        this.defaultShader = core.getRenderer().getShader();
        this.particleShader = new ParticleInstancedShader();
        this.particleShader.load();
        this.particleShader.init();
        quad = MeshLoader.createParticleCenteredQuad();
        quad.addInstancedAttribute(2, 1, 4, INSTANCE_DATA_LENGTH, 0);
        quad.addInstancedAttribute(2, 2, 4, INSTANCE_DATA_LENGTH, 4);
        quad.addInstancedAttribute(2, 3, 4, INSTANCE_DATA_LENGTH, 8);
        quad.addInstancedAttribute(2, 4, 4, INSTANCE_DATA_LENGTH, 12);
        quad.addInstancedAttribute(2, 5, 4, INSTANCE_DATA_LENGTH, 16);
        quad.addInstancedAttribute(2, 6, 1, INSTANCE_DATA_LENGTH, 20);
        quad.addInstancedAttribute(2, 7, 4, INSTANCE_DATA_LENGTH, 21);
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

        HashMap<Texture, List<Particle>> hashMapByTexture = particlesHashMap.get(renderType);
        List<Particle> particlesByTexture = hashMapByTexture.get(particle.getTexture());
        particlesByTexture.remove(particle);
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

        if (EnumOption.IS_DEBUG.getBoolean()) {
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

    private void renderParticles(List<Particle> particles, Texture texture) {
        int numberOfRows = texture.getNumberOfRows();
        texture.bind();

        if (particles.size() > 0) {
            Particle p = particles.get(0);
            OpenGLHelper.alphaGreater(p.getGreater());
        }

        if (numberOfRows > 0) {
            particleShader.setAnimatedTexture(true);
            particleShader.setNumberOfRows(numberOfRows);
        } else {
            particleShader.setAnimatedTexture(false);
        }

        particleShader.enableTexture();

        int size = particles.size() * INSTANCE_DATA_LENGTH;

        buffer.clear();

        while (buffer.capacity() < size) {
            buffer = BufferUtils.createFloatBuffer(buffer.capacity() << 1);
        }

        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            storeMatrixData(Transformation.getModelViewMatrix(particle));
            storeParticleData(particle);
        }

        quad.updateVertexBuffer(2, buffer.flip(), INSTANCE_DATA_LENGTH);
        GL30C.glBindVertexArray(quad.getVaoId());
        GL31C.glDrawElementsInstanced(GL11C.GL_TRIANGLES, quad.getIndexCount(), GL11C.GL_UNSIGNED_INT, 0, particles.size());
        Renderer renderer = core.getRenderer();
        renderer.increaseDrawCalls();
    }

    private void storeParticleData(Particle particle) {
        if (particle instanceof ParticleAnimated animatedP) {
            buffer.put(animatedP.getTextureOffset1().x);
            buffer.put(animatedP.getTextureOffset1().y);
            buffer.put(animatedP.getTextureOffset2().x);
            buffer.put(animatedP.getTextureOffset2().y);
            buffer.put(animatedP.getBlend());
        } else {
            buffer.put(0.1f);
            buffer.put(0.1f);
            buffer.put(0.1f);
            buffer.put(0.1f);
            buffer.put(0.1f);
        }

        Vector4f color = particle.getColor();
        buffer.put(color.x);
        buffer.put(color.y);
        buffer.put(color.z);
        buffer.put(color.w);
    }

    private void storeMatrixData(Matrix4f matrix) {
        buffer.put(matrix.m00());
        buffer.put(matrix.m01());
        buffer.put(matrix.m02());
        buffer.put(matrix.m03());
        buffer.put(matrix.m10());
        buffer.put(matrix.m11());
        buffer.put(matrix.m12());
        buffer.put(matrix.m13());
        buffer.put(matrix.m20());
        buffer.put(matrix.m21());
        buffer.put(matrix.m22());
        buffer.put(matrix.m23());
        buffer.put(matrix.m30());
        buffer.put(matrix.m31());
        buffer.put(matrix.m32());
        buffer.put(matrix.m33());
    }

    public void render(EnumParticlePositionType positionType) {
        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        //render wrecks
        if (positionType == EnumParticlePositionType.Default) {
            defaultShader.enable();
            for (Texture texture : particlesWrecksHashMap.keySet()) {
                renderParticlesWrecks(particlesWrecksHashMap.get(texture));
            }
        }

        particleShader.enable();

        if (positionType == EnumParticlePositionType.Background) particleShader.setOrthoMatrix(core.getRenderer().getCamera().getOrthographicMatrix());

        String renderType = EnumParticleRenderType.AlphaBlended + " " + positionType.toString();
        HashMap<Texture, List<Particle>> hashMapByTexture = particlesHashMap.get(renderType);
        if (hashMapByTexture != null) {
            for (Texture texture : hashMapByTexture.keySet()) {
                renderParticles(hashMapByTexture.get(texture), texture);
            }
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
        hashMapByTexture = particlesHashMap.get(renderType);
        if (hashMapByTexture != null) {
            for (Texture texture : hashMapByTexture.keySet()) {
                renderParticles(hashMapByTexture.get(texture), texture);
            }
        }

        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    void addParticle(ParticleWreck particle) {
        Texture texture = particle.getTexture();
        List<ParticleWreck> particles = particlesWrecksHashMap.get(texture);

        if (particles == null) {
            particles = new ArrayList<>();
        }

        particles.add(particle);
        particlesWrecksHashMap.put(texture, particles);
        particlesWrecks.add(particle);
    }

    void addParticle(Particle particle) {
        EnumParticlePositionType positionType = particle.getPositionType();
        EnumParticleRenderType renderType = particle.getRenderType();
        Texture texture = particle.getTexture();

        String fullRenderType = renderType.toString() + " " + positionType.toString();
        HashMap<Texture, List<Particle>> hashMapByTexture = particlesHashMap.get(fullRenderType);
        if (hashMapByTexture == null) {
            hashMapByTexture = new HashMap<>();
        }

        List<Particle> particles = hashMapByTexture.get(texture);
        if (particles == null) {
            particles = new ArrayList<>();
        }

        particles.add(particle);
        hashMapByTexture.put(texture, particles);
        particlesHashMap.put(fullRenderType, hashMapByTexture);
        this.particles.add(particle);
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public List<ParticleWreck> getParticlesWrecks() {
        return particlesWrecks;
    }
}
