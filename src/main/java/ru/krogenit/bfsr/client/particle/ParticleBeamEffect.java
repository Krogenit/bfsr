package ru.krogenit.bfsr.client.particle;

import java.util.Random;

import org.joml.Vector2f;
import org.joml.Vector4f;

import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.component.weapon.WeaponSlotBeam;
import ru.krogenit.bfsr.entity.ship.Ship;

public class ParticleBeamEffect extends Particle {
	private final WeaponSlotBeam slot;
	private final Ship ship;
	private final Vector2f addPos;
	private final Vector2f addScale;
	private final Random rand;
	private boolean changeColor;
	
	public ParticleBeamEffect(WeaponSlotBeam slot, TextureRegister text) {
		super(text, new Vector2f(), new Vector2f(), 0f, 0f, new Vector2f(), 0f, new Vector4f(), 0f, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
		this.slot = slot;
		this.ship = slot.getShip();
		this.rand = ship.getWorld().getRand();
		this.color = new Vector4f(slot.getBeamColor());
		Vector2f slotScale = slot.getScale();
		this.addPos = new Vector2f(rand.nextFloat(), (rand.nextFloat() * 2f - 1f) * slotScale.y/2f);
		this.addScale = new Vector2f(50f + 28f * rand.nextFloat(), slotScale.y / 2f + 4f * rand.nextFloat());
	}
	
	@Override
	public void update(double delta) {
		float beamRange = slot.getCurrentBeamRange();
		Vector2f slotPos = slot.getPosition();
		Vector4f beamColor = slot.getBeamColor();
		
		float cos = ship.getCos();
		float sin = ship.getSin();
		
		float l = beamRange * addPos.x + (rand.nextFloat() * 2f - 1f);
		float k = addPos.y;
		Vector2f pos = new Vector2f(cos * l - sin * k, sin * l + cos * k);
		pos.x += slotPos.x;
		pos.y += slotPos.y;
		
		rotate = slot.getRotation();
		position.x = pos.x;
		position.y = pos.y;
		scale.x = addScale.x;
		scale.y = addScale.y;
		
		float colorSpeed = (float) (15f * rand.nextFloat() * delta);
		if(changeColor) {
			if(color.w > 0) {
				color.w -= colorSpeed;
			}
		} else {
			if(color.w < beamColor.w*2f) {
				color.w += colorSpeed;
			}  else {
				changeColor = true;
			}
		}
		
		if(color.w > beamColor.w*2f)
			color.w = beamColor.w*2f;
		
		if(ship.isDead() || color.w <= 0) {
			setDead(true);
		}
	}
}
