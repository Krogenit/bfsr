package net.bfsr.client.particle;

import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.entity.ship.Ship;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class ParticleBeam extends Particle {
    private WeaponSlotBeam slot;
    private Ship ship;
    private boolean isSmall;

    public ParticleBeam init(WeaponSlotBeam slot, boolean isSmall, TextureRegister texture) {
        init(texture, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
        this.slot = slot;
        this.ship = slot.getShip();
        this.isSmall = isSmall;
        return this;
    }

    @Override
    public void update() {
        lastPosition.set(getPosition());
        Vector2f slotScale = slot.getScale();
        Vector2f slotPos = slot.getPosition();
        Vector4f beamColor = slot.getBeamColor();
        this.color.x = beamColor.x;
        this.color.y = beamColor.y;
        this.color.z = beamColor.z;
        this.color.w = beamColor.w;

        this.rotation = slot.getRotation();

        float cos = ship.getCos();
        float sin = ship.getSin();
        float startRange = -slotScale.x;

        float startx = cos * startRange;
        float starty = sin * startRange;

        float posx;
        float posy;

        Vector2f collisionPoint = slot.getCollisionPoint();
        if (collisionPoint.x != 0 || collisionPoint.y != 0) {
            startRange = -slotScale.x / 2f;

            startx = cos * startRange;
            starty = sin * startRange;
            posx = collisionPoint.x;
            posy = collisionPoint.y;
            startx += slotPos.x;
            starty += slotPos.y;
            position.x = (startx + collisionPoint.x) / 2f;
            position.y = (starty + collisionPoint.y) / 2f;
        } else {
            float beamMaxRange = slot.getBeamMaxRange();
            posx = (startx + cos * beamMaxRange);
            posy = (starty + sin * beamMaxRange);
            position.x = slotPos.x + posx / 2f;
            position.y = slotPos.y + posy / 2f;
        }

        scale.x = (float) Math.sqrt((posx - startx) * (posx - startx) + (posy - starty) * (posy - starty));
        scale.y = slotScale.y;
        if (isSmall) {
            scale.y /= 3f;
        } else {
            color.w /= 3f;
        }

        if (color.w <= 0 || ship.isDead()) {
            setDead(true);
        }
    }

    @Override
    public void returnToPool() {
        ParticleSpawner.PARTICLE_BEAM_POOL.returnBack(this);
    }
}
