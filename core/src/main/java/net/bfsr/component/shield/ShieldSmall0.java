package net.bfsr.component.shield;

import net.bfsr.client.loader.TextureLoader;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class ShieldSmall0 extends Shield {

    public ShieldSmall0(Ship ship, Vector4f color, float maxShield, float shieldRegen, float rebuildTime) {
        super(ship);
        setMaxShield(maxShield);
        setShield(maxShield);
        setShieldRegen(shieldRegen);
        setTimeToRebuild(rebuildTime);
        setScale(new Vector2f(80, 80));

        if (ship.getWorld().isRemote()) {
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

        if (ship.getWorld().isRemote()) {
            setColor(color);
            setTexture(TextureLoader.getTexture(TextureRegister.shieldStation0));
        }

        createBody(ship.getPosition());
    }

}
