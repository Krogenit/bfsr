package net.bfsr.client.renderer.particle;


import net.bfsr.client.particle.ParticleBeamEffect;
import net.bfsr.client.particle.ParticleManager;
import net.bfsr.component.weapon.WeaponSlotBeam;

public class ParticleBeamRender extends ParticleRender {
    private WeaponSlotBeam slot;
    private boolean changeColor;

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
        this.changeColor = false;
        this.slot = object.getSlot();
        return this;
    }

    @Override
    public void update() {
        lastPosition.set(object.getPosition());
        lastSin = object.getSin();
        lastCos = object.getCos();

        float beamPower = slot.getBeamPower();
        float colorSpeed = 0.25f * ParticleManager.RAND.nextFloat();
        if (changeColor) {
            if (color.w > 0) {
                color.w -= colorSpeed;
            }
        } else {
            float maxAlphaColor = beamPower * 2.0f;

            if (color.w < maxAlphaColor) {
                color.w += colorSpeed;
                if (color.w >= maxAlphaColor) {
                    color.w = maxAlphaColor;
                    changeColor = true;
                }
            } else {
                changeColor = true;
            }
        }

        if (color.w <= 0) {
            object.setDead();
        }
    }
}