package net.bfsr.component.shield;

import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import org.joml.Vector2f;

public class ShieldSmall0 extends Shield {
    public ShieldSmall0(Ship ship, float r, float g, float b, float a, float maxShield, float shieldRegen, float rebuildTime) {
        super(ship);
        setMaxShield(maxShield);
        setShield(maxShield);
        setShieldRegen(shieldRegen);
        setTimeToRebuild(rebuildTime);

        if (ship.getWorld().isRemote()) {
            setColor(r, g, b, a);
            setTexture(TextureLoader.getTexture(TextureRegister.shieldStation0));
        }

        Vector2f position = ship.getPosition();
        createBody(position.x, position.y);
    }

    public ShieldSmall0(Ship ship, float r, float g, float b, float a) {
        super(ship);
        setMaxShield(15);
        setShield(15);
        setShieldRegen(0.6f);
        setTimeToRebuild(200);

        if (ship.getWorld().isRemote()) {
            setColor(r, g, b, a);
            setTexture(TextureLoader.getTexture(TextureRegister.shieldStation0));
        }

        Vector2f position = ship.getPosition();
        createBody(position.x, position.y);
    }
}
