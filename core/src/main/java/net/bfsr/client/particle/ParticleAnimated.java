package net.bfsr.client.particle;

import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.ParticleShader;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class ParticleAnimated extends Particle {

    private Vector2f textureOffset1, textureOffset2;
    private float blend;
    private final float startColor;

    public ParticleAnimated(TextureRegister text, Vector2f pos, float rot, float rotSpeed, Vector2f velocity, Vector4f color, Vector2f size,
                            float sizeSpeed, float alphaSpeed, boolean isCollidable, float greater, EnumParticlePositionType positionType, EnumParticleRenderType renderType) {
        super(text, pos, velocity, rot, rotSpeed, size, sizeSpeed, color, alphaSpeed, greater, false, isCollidable, positionType, renderType);

        this.startColor = color.w;
    }

    @Override
    public void update() {
        super.update();
        updateTextureCoords();
    }

    @Override
    public void renderParticle(ParticleShader shader) {
        shader.setTextureCoordInfo(textureOffset1, textureOffset2, texture.getNumberOfRows(), blend);
        super.render(shader);
    }

    private void updateTextureCoords() {
        float lifeFactor = 0;

        if (alphaVelocity != 0) {
            lifeFactor = 1f - color.w / startColor;
        }

        int stageCount = texture.getNumberOfRows() * texture.getNumberOfRows();
        float atlasProgression = lifeFactor * stageCount;
        int index1 = (int) Math.floor(atlasProgression);
        int index2 = index1 < stageCount - 1 ? index1 + 1 : index1;
        this.blend = atlasProgression % 1;
        setTextureOffset(textureOffset1, index1);
        setTextureOffset(textureOffset2, index2);
    }

    private void setTextureOffset(Vector2f offset, int index) {
        int column = index % texture.getNumberOfRows();
        int row = index / texture.getNumberOfRows();
        offset.x = column / (float) texture.getNumberOfRows();
        offset.y = row / (float) texture.getNumberOfRows();
    }

    public Vector2f getTextureOffset1() {
        return textureOffset1;
    }

    public Vector2f getTextureOffset2() {
        return textureOffset2;
    }

    public float getBlend() {
        return blend;
    }
}
