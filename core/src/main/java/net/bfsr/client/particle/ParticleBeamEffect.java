package net.bfsr.client.particle;

import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.entity.ship.Ship;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class ParticleBeamEffect extends Particle {
    private final WeaponSlotBeam slot;
    private final Ship ship;
    private final Vector2f addPos;
    private final Vector2f addScale;
    private final Random rand;
    private boolean changeColor;

    ParticleBeamEffect(WeaponSlotBeam slot, TextureRegister text) {
        super(text, new Vector2f(), new Vector2f(), 0.0f, 0.0f, new Vector2f(), 0.0f, new Vector4f(), 0.0f, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
        this.slot = slot;
        ship = slot.getShip();
        rand = ship.getWorld().getRand();
        color = new Vector4f(slot.getBeamColor());
        Vector2f slotScale = slot.getScale();
        addPos = new Vector2f(rand.nextFloat(), (rand.nextFloat() * 2.0f - 1.0f) * slotScale.y / 2.0f);
        addScale = new Vector2f(5.0f + 2.8f * rand.nextFloat(), slotScale.y / 2.0f + 0.4f * rand.nextFloat());
    }

    @Override
    public void update() {
        float beamRange = slot.getCurrentBeamRange();
        Vector2f slotPos = slot.getPosition();
        Vector4f beamColor = slot.getBeamColor();

        float cos = ship.getCos();
        float sin = ship.getSin();

        float l = beamRange * addPos.x + (rand.nextFloat() * 2.0f - 1.0f);
        float k = addPos.y;
        Vector2f pos = new Vector2f(cos * l - sin * k, sin * l + cos * k);
        pos.x += slotPos.x;
        pos.y += slotPos.y;

        rotate = slot.getRotation();
        position.x = pos.x;
        position.y = pos.y;
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
}
