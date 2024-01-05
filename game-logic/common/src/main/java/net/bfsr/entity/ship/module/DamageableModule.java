package net.bfsr.entity.ship.module;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.event.EventBus;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.module.ModuleDestroyEvent;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;

@NoArgsConstructor
public abstract class DamageableModule extends Module {
    @Getter
    protected float maxHp;
    @Getter
    @Setter
    protected float hp;
    @Getter
    protected BodyFixture fixture;
    private EventBus eventBus;
    @Getter
    private Ship ship;

    protected DamageableModule(float hp) {
        this.maxHp = this.hp = hp;
    }

    protected DamageableModule(float hp, float sizeX, float sizeY) {
        super(sizeX, sizeY);
        this.maxHp = this.hp = hp;
    }

    protected DamageableModule(float x, float y, float sizeX, float sizeY) {
        super(x, y, sizeX, sizeY);
    }

    public void init(Ship ship) {
        this.ship = ship;
        this.eventBus = ship.getWorld().getEventBus();
        createFixture();
    }

    protected void createFixture() {}

    public void addFixtureToBody(Body body) {
        if (isDead) return;

        body.addFixture(fixture);
    }

    public boolean damage(float amount) {
        hp -= amount;

        if (hp <= 0 && !isDead) {
            setDead();
        }

        return true;
    }

    @Override
    public void setDead() {
        super.setDead();
        destroy();
    }

    protected void destroy() {
        eventBus.publish(new ModuleDestroyEvent(this));
    }
}