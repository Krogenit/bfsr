package ru.krogenit.bfsr.component.shield;

import org.joml.Vector2f;
import org.joml.Vector4f;

import ru.krogenit.bfsr.client.loader.TextureLoader;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.entity.ship.Ship;

public class ShieldSmall0 extends Shield {
	
	public ShieldSmall0(Ship ship, Vector4f color, float maxShield, float shieldRegen, float rebuildTime) {
		super(ship);
		setMaxShield(maxShield);
		setShield(maxShield);
		setShieldRegen(shieldRegen);
		setTimeToRebuild(rebuildTime);
		setScale(new Vector2f(80, 80));
		
		if(ship.getWorld().isRemote()) {
			setColor(color);
			setTexture(TextureLoader.getTexture(TextureRegister.shieldStation0));
		}

		createBody(ship.getPosition());
	}

	public ShieldSmall0(Ship ship, Vector4f color) {
		super(ship);
		setMaxShield(15);
		setShield(15);
		setShieldRegen(0.6f);
		setTimeToRebuild(200);
		setScale(new Vector2f(80, 80));
		
		if(ship.getWorld().isRemote()) {
			setColor(color);
			setTexture(TextureLoader.getTexture(TextureRegister.shieldStation0));
		}

		createBody(ship.getPosition());
	}

}
