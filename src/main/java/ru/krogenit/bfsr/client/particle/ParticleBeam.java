package ru.krogenit.bfsr.client.particle;

import org.joml.Vector2f;
import org.joml.Vector4f;

import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.component.weapon.WeaponSlotBeam;
import ru.krogenit.bfsr.entity.ship.Ship;

public class ParticleBeam extends Particle {

	private final WeaponSlotBeam slot;
	private final Ship ship;
	private final boolean isSmall;
	
	public ParticleBeam(WeaponSlotBeam slot, boolean isSmall, TextureRegister text) {
		super(text, new Vector2f(), new Vector2f(), 0f, 0f, new Vector2f(), 0f, new Vector4f(), 0f, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
		this.slot = slot;
		this.ship = slot.getShip();
		this.isSmall = isSmall;
	}

	@Override
	public void update(double delta) {
		Vector2f slotScale = slot.getScale();
		Vector2f slotPos = slot.getPosition();
		Vector4f beamColor = slot.getBeamColor();
		this.color.x = beamColor.x;
		this.color.y = beamColor.y;
		this.color.z = beamColor.z;
		this.color.w = beamColor.w;
		
		this.rotate = slot.getRotation();
		
		float cos = ship.getCos();
		float sin = ship.getSin();
		float startRange = -slotScale.x;

		float startx = cos * startRange;
		float starty = sin * startRange;
		
		float posx;
		float posy;
		
		Vector2f collisionPoint = slot.getCollisionPoint();
		if(collisionPoint.x != 0 || collisionPoint.y != 0) {
			startRange = -slotScale.x/2f;

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

		scale.x = (float) Math.sqrt((posx - startx)  * (posx - startx) + (posy - starty) * (posy - starty));
		scale.y = slotScale.y;
		if(isSmall) {
			scale.y /= 3f;
		} else {
			color.w /= 3f;
		}
		
		if(color.w <= 0 || ship.isDead()) {
			setDead(true);
		}
	}
}
