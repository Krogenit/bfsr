package net.bfsr.client.renderer.particle;


import net.bfsr.client.particle.ParticleBeamEffect;
import net.bfsr.client.particle.ParticleManager;
import org.joml.Vector4f;

public class ParticleBeamRender extends ParticleRender {
    private boolean fadeIn;
    private Vector4f beamColor;

    public ParticleBeamRender init(ParticleBeamEffect object, long textureHandle, float r, float g, float b, float a) {
        this.object = object;
        this.position = object.getPosition();
        this.size = object.getSize();
        this.lastPosition.set(position);
        this.lastSin = object.getSin();
        this.lastCos = object.getCos();
        this.lastSize.set(size);
        this.color.set(r, g, b, a);
        this.lastColor.set(color);
        this.textureHandle = textureHandle;
        this.fadeIn = true;
        this.beamColor = object.getBeamColor();
        return this;
    }

    @Override
    public void update() {
        lastPosition.set(object.getPosition());
        lastSin = object.getSin();
        lastCos = object.getCos();

        float colorSpeed = 0.25f * ParticleManager.RAND.nextFloat();
        if (fadeIn) {
            float maxAlphaColor = beamColor.w * 2.0f;

            if (color.w < maxAlphaColor) {
                color.w += colorSpeed;
                if (color.w >= maxAlphaColor) {
                    color.w = maxAlphaColor;
                    fadeIn = false;
                }
            } else {
                fadeIn = false;
            }
        } else {
            if (color.w > 0) {
                color.w -= colorSpeed;
            }
        }

        if (color.w > beamColor.w * 2.0f) {
            color.w = beamColor.w * 2.0f;
        }

        if (color.w <= 0) {
            object.setDead();
        }
    }
}