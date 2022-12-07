package net.bfsr.client.particle;

import lombok.Getter;
import net.bfsr.client.camera.Camera;
import net.bfsr.client.loader.MeshLoader;
import net.bfsr.client.model.Mesh;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.ParticleInstancedShader;
import net.bfsr.client.texture.Texture;
import net.bfsr.core.Core;
import net.bfsr.math.Transformation;
import net.bfsr.world.WorldClient;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParticleRenderer {
    @Getter
    private static ParticleRenderer instance;

    private final Core core = Core.getCore();
    private final WorldClient world;
    private final Camera cam;

    private final BaseShader defaultShader;
    private final ParticleInstancedShader particleShader;

    private final HashMap<String, HashMap<Texture, List<Particle>>> particlesHashMap = new HashMap<>();
    private final List<Particle> particles = new ArrayList<>();

    private final HashMap<Texture, List<ParticleWreck>> particlesWrecksHashMap = new HashMap<>();
    private final List<ParticleWreck> particlesWrecks = new ArrayList<>();

    private static final float[] VERTICES = {-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f};
    private static final int MAX_INSTANCES = 10000;
    private static final int INSTANCE_DATA_LENGTH = 25;

    private final MeshLoader meshLoader;
    private final Mesh quad;
    private final int vbo;
    private int pointer;

    public ParticleRenderer(WorldClient w) {
        this.world = w;
        this.cam = core.getRenderer().getCamera();
        this.defaultShader = core.getRenderer().getShader();
        this.particleShader = new ParticleInstancedShader();
        this.particleShader.load();
        this.particleShader.init();
        this.meshLoader = new MeshLoader();
        this.quad = new Mesh(VERTICES);
        this.vbo = meshLoader.createEmptyVbo(INSTANCE_DATA_LENGTH * MAX_INSTANCES);

        this.meshLoader.addInstancedAttribute(quad.getVaoId(), vbo, 1, 4, INSTANCE_DATA_LENGTH, 0);
        this.meshLoader.addInstancedAttribute(quad.getVaoId(), vbo, 2, 4, INSTANCE_DATA_LENGTH, 4);
        this.meshLoader.addInstancedAttribute(quad.getVaoId(), vbo, 3, 4, INSTANCE_DATA_LENGTH, 8);
        this.meshLoader.addInstancedAttribute(quad.getVaoId(), vbo, 4, 4, INSTANCE_DATA_LENGTH, 12);
        this.meshLoader.addInstancedAttribute(quad.getVaoId(), vbo, 5, 4, INSTANCE_DATA_LENGTH, 16);
        this.meshLoader.addInstancedAttribute(quad.getVaoId(), vbo, 6, 1, INSTANCE_DATA_LENGTH, 20);
        this.meshLoader.addInstancedAttribute(quad.getVaoId(), vbo, 7, 4, INSTANCE_DATA_LENGTH, 21);
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

    private void renderParticlesWrecks(List<ParticleWreck> particles, Texture texture) {
        if (particles.size() > 0) {
            Particle p = particles.get(0);
            OpenGLHelper.alphaGreater(p.getGreater());
        }

        for (ParticleWreck p : particles) {
            if (p.getAABB().isIntersects(cam.getBoundingBox()))
                p.render(defaultShader);
        }

        if (core.getSettings().isDebug()) {
            core.getRenderer().getCamera().setupOpenGLMatrix();

            GL20.glUseProgram(0);
            for (ParticleWreck particle : particles) {
                particle.renderDebug();
            }
            defaultShader.enable();
        }
    }

    private void renderParticlesWrecksEffects(List<ParticleWreck> paritcles, Texture texture) {
        for (ParticleWreck paritcle : paritcles) {
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
        } else particleShader.setAnimatedTexture(false);
        particleShader.enableTexture();

        this.pointer = 0;
        float[] vboData = new float[particles.size() * INSTANCE_DATA_LENGTH];
        GL30.glBindVertexArray(quad.getVaoId());
        int attributesCount = 8;
        for (int i = 0; i < attributesCount; i++)
            GL20.glEnableVertexAttribArray(i);

        for (Particle particle : particles) {
            storeMatrixData(Transformation.getModelViewMatrix(particle), vboData);
            storeParticleData(particle, vboData);
        }
        meshLoader.updateVbo(vbo, vboData);
        GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount(), particles.size());
        Renderer renderer = core.getRenderer();
        renderer.setDrawCalls(renderer.getDrawCalls() + 1);
        for (int i = 0; i < attributesCount; i++)
            GL20.glDisableVertexAttribArray(i);
        GL30.glBindVertexArray(0);
    }

    private void storeParticleData(Particle particle, float[] data) {
        if (particle instanceof ParticleAnimated) {
            ParticleAnimated animatedP = (ParticleAnimated) particle;
            data[pointer++] = animatedP.getTextureOffset1().x;
            data[pointer++] = animatedP.getTextureOffset1().y;
            data[pointer++] = animatedP.getTextureOffset2().x;
            data[pointer++] = animatedP.getTextureOffset2().y;
            data[pointer++] = animatedP.getBlend();
        } else {
            data[pointer++] = 0.1f;
            data[pointer++] = 0.1f;
            data[pointer++] = 0.1f;
            data[pointer++] = 0.1f;
            data[pointer++] = 0.1f;
        }

        Vector4f color = particle.getColor();
        data[pointer++] = color.x;
        data[pointer++] = color.y;
        data[pointer++] = color.z;
        data[pointer++] = color.w;
    }

    private void storeMatrixData(Matrix4f matrix, float[] vboData) {
        vboData[pointer++] = matrix.m00();
        vboData[pointer++] = matrix.m01();
        vboData[pointer++] = matrix.m02();
        vboData[pointer++] = matrix.m03();
        vboData[pointer++] = matrix.m10();
        vboData[pointer++] = matrix.m11();
        vboData[pointer++] = matrix.m12();
        vboData[pointer++] = matrix.m13();
        vboData[pointer++] = matrix.m20();
        vboData[pointer++] = matrix.m21();
        vboData[pointer++] = matrix.m22();
        vboData[pointer++] = matrix.m23();
        vboData[pointer++] = matrix.m30();
        vboData[pointer++] = matrix.m31();
        vboData[pointer++] = matrix.m32();
        vboData[pointer++] = matrix.m33();
    }

    public void render(EnumParticlePositionType positionType) {
        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        //render wrecks
        if (positionType == EnumParticlePositionType.Default) {
            defaultShader.enable();
            for (Texture texture : particlesWrecksHashMap.keySet()) {
                renderParticlesWrecks(particlesWrecksHashMap.get(texture), texture);
            }
        }

        particleShader.enable();

        if (positionType == EnumParticlePositionType.Background) particleShader.setOrthoMatrix(core.getRenderer().getCamera().getOrthographicMatrix());

        String renderType = EnumParticleRenderType.AlphaBlended.toString() + " " + positionType.toString();
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
                renderParticlesWrecksEffects(particlesWrecksHashMap.get(texture), texture);
            }
        }

        particleShader.enable();

        renderType = EnumParticleRenderType.Additive.toString() + " " + positionType.toString();
        hashMapByTexture = particlesHashMap.get(renderType);
        if (hashMapByTexture != null) {
            for (Texture texture : hashMapByTexture.keySet()) {
                renderParticles(hashMapByTexture.get(texture), texture);
            }
        }

        OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void addParticle(ParticleWreck particle) {
        Texture texture = particle.getTexture();
        List<ParticleWreck> particles = particlesWrecksHashMap.get(texture);

        if (particles == null) {
            particles = new ArrayList<>();
        }

        particles.add(particle);
        particlesWrecksHashMap.put(texture, particles);
        particlesWrecks.add(particle);
    }

    public void addParticle(Particle particle) {
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
