package net.bfsr.module;

import net.bfsr.engine.logic.Logic;
import net.bfsr.entity.ship.module.shield.Shield;

public class CommonShieldLogic implements Logic {
    public void update(Shield shield) {
        if (shield.getShieldHp() < shield.getShieldMaxHp() && shield.isAlive()) {
            shield.regenHp();
            onShieldAlive(shield);
        }
    }

    protected void onShieldAlive(Shield shield) {}

    public void onShieldRemove(Shield shield) {}

    public void onRebuildingTimeUpdate(Shield shield) {}
}