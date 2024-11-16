package net.bfsr.client.module;

import net.bfsr.engine.Engine;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.module.CommonShieldLogic;

public class ShieldLogic extends CommonShieldLogic {
    private final float scaleAnimation = Engine.convertToDeltaTime(3.6f);

    @Override
    protected void onShieldAlive(Shield shield) {
        float sizeX = shield.getSizeX();
        if (sizeX < 1.0f) {
            shield.addSize(scaleAnimation, 0);
            if (shield.getSizeX() > 1.0f) shield.setSize(1.0f, shield.getSizeY());
        }
    }
}