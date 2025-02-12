package net.bfsr.entity.ship.module.shield;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.shield.ShieldData;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.module.CommonShieldLogic;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.Filters;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Fixture;

public class Shield extends DamageableModule {
    private final float shieldRegen;
    @Getter
    private final int timeToRebuild;
    @Setter
    @Getter
    private int rebuildingTime;
    @Getter
    private boolean alive;
    @Getter
    @Setter
    private float shieldHp;
    @Getter
    private final float shieldMaxHp;
    private final Shape shieldShape;
    private final CommonShieldLogic logic;

    public Shield(ShieldData shieldData, Shape shieldShape, CommonShieldLogic logic) {
        super(shieldData, 5.0f, 1.0f, 1.0f);
        this.shieldHp = shieldMaxHp = shieldData.getMaxShield();
        this.shieldRegen = shieldData.getRegenAmount();
        this.timeToRebuild = shieldData.getRebuildTimeInTicks();
        this.rebuildingTime = timeToRebuild;
        this.shieldShape = shieldShape;
        this.logic = logic;
        this.alive = true;
    }

    @Override
    public void createFixture(RigidBody rigidBody) {
        fixture = new Fixture(shieldShape, Filters.SHIP_FILTER, this, PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        rigidBody.addFixture(fixture);
    }

    @Override
    public void update() {
        if (isDead) {
            return;
        }

        logic.update(this);
    }

    public void rebuilding() {
        rebuildingTime += 1;
    }

    public void regenHp() {
        if (shieldHp < shieldMaxHp) {
            shieldHp += shieldRegen;
            if (shieldHp > shieldMaxHp) {
                shieldHp = shieldMaxHp;
            }
        }
    }

    public void rebuildShield() {
        shieldHp = shieldMaxHp / 5.0f;
        rebuildingTime = timeToRebuild;
        alive = true;
    }

    public void damageShield(float amount) {
        shieldHp -= amount;

        if (shieldHp <= 0) {
            removeShield();
            shieldHp = 0;
        }
    }

    @Override
    protected void destroy() {
        super.destroy();
        removeShield();
        ship.removeFixture(fixture);
    }

    public void resetRebuildingTime() {
        rebuildingTime = 0;
        logic.onRebuildingTimeUpdate(this);
    }

    public void removeShield() {
        rebuildingTime = 0;
        setSize(0.0f, 0.0f);
        shieldHp = 0;
        alive = false;
        logic.onShieldRemove(this);
    }

    @Override
    public ModuleType getType() {
        return ModuleType.SHIELD;
    }
}