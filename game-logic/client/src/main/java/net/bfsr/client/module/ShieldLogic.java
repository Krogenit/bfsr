package net.bfsr.client.module;

import net.bfsr.engine.Engine;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.module.CommonShieldLogic;
import org.joml.Vector2f;

public class ShieldLogic extends CommonShieldLogic {
    private final float scaleAnimation = Engine.convertToDeltaTime(3.6f);

    @Override
    protected void onShieldAlive(Shield shield) {
        Vector2f size = shield.getSize();
        if (size.x < 1.0f) {
            size.x += scaleAnimation;
            if (size.x > 1.0f) size.x = 1.0f;
        }
    }
}