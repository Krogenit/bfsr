package net.bfsr.client.particle;

import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.entity.ship.Ship;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class ParticleBeamEffect extends Particle {
    private WeaponSlotBeam slot;
    private Ship ship;
    private final Vector2f addPos = new Vector2f();
    private final Vector2f addScale = new Vector2f();
    private Random rand;
    private boolean changeColor;

    public ParticleBeamEffect init(WeaponSlotBeam slot, TextureRegister texture) {
        init(texture, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
        this.slot = slot;
        ship = slot.getShip();
        rand = ship.getWorld().getRand();
        color.set(slot.getBeamColor());
        Vector2f slotScale = slot.getScale();
        addPos.set(rand.nextFloat(), (rand.nextFloat() * 2.0f - 1.0f) * slotScale.y / 2.0f);
        addScale.set(5.0f + 2.8f * rand.nextFloat(), slotScale.y / 2.0f + 0.4f * rand.nextFloat());
        return this;
    }

    @Override
    public void update() {
        lastPosition.set(position);
        float beamRange = slot.getCurrentBeamRange();
        Vector2f slotPos = slot.getPosition();
        Vector4f beamColor = slot.getBeamColor();

        float cos = ship.getCos();
        float sin = ship.getSin();

        float l = beamRange * addPos.x + (rand.nextFloat() * 2.0f - 1.0f);
        float k = addPos.y;

        rotation = slot.getRotation();
        position.x = cos * l - sin * k + slotPos.x;
        position.y = sin * l + cos * k + slotPos.y;
        scale.x = addScale.x;
        scale.y = addScale.y;

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

    @Override
    public void returnToPool() {
        ParticleSpawner.PARTICLE_BEAM_EFFECT_POOL.returnBack(this);
    }
}
