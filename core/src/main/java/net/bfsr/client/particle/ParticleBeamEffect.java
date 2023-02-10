package net.bfsr.client.particle;

import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class ParticleBeamEffect extends Particle {
    private WeaponSlotBeam slot;
    private Ship ship;
    private final Vector2f addPos = new Vector2f();
    private Random rand;
    private boolean changeColor;

    public ParticleBeamEffect init(WeaponSlotBeam slot, TextureRegister texture) {
        init(texture, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5.0f + 2.8f * slot.getShip().getWorld().getRand().nextFloat(),
                slot.getScale().y / 2.0f + 0.4f * slot.getShip().getWorld().getRand().nextFloat(), 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, RenderLayer.DEFAULT_ADDITIVE);
        this.slot = slot;
        this.ship = slot.getShip();
        this.rand = ship.getWorld().getRand();
        this.color.set(slot.getBeamColor());
        this.lastColor.set(color);
        this.addPos.set(rand.nextFloat(), (rand.nextFloat() * 2.0f - 1.0f) * slot.getScale().y / 2.0f);
        calculateTransform();
        this.lastPosition.set(position);
        return this;
    }

    @Override
    public void update() {
        lastPosition.set(position);
        lastRotation = rotation;

        calculateTransform();

        Vector4f beamColor = slot.getBeamColor();
        float colorSpeed = 0.25f * rand.nextFloat();
        if (changeColor) {
            if (color.w > 0) {
                color.w -= colorSpeed;
            }
        } else {
            if (color.w < beamColor.w * 2.0f) {
                color.w += colorSpeed;
            } else {
                changeColor = true;
            }
        }

        if (color.w > beamColor.w * 2.0f)
            color.w = beamColor.w * 2.0f;

        if (ship.isDead() || color.w <= 0) {
            setDead(true);
        }
    }

    public void calculateTransform() {
        float beamRange = slot.getCurrentBeamRange();
        Vector2f slotPos = slot.getPosition();

        float cos = ship.getCos();
        float sin = ship.getSin();

        float l = beamRange * addPos.x + (rand.nextFloat() * 2.0f - 1.0f);
        float k = addPos.y;

        rotation = slot.getRotation();
        position.x = cos * l - sin * k + slotPos.x;
        position.y = sin * l + cos * k + slotPos.y;
    }

    @Override
    public void onRemoved() {
        ParticleSpawner.PARTICLE_BEAM_EFFECT_POOL.returnBack(this);
        Core.get().getRenderer().getParticleRenderer().removeParticleFromRenderLayer(this, renderLayer);
    }
}
