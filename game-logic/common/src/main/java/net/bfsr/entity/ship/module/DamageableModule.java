package net.bfsr.entity.ship.module;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.event.EventBus;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.module.ModuleDestroyEvent;
import org.jbox2d.dynamics.Fixture;

@NoArgsConstructor
public abstract class DamageableModule extends Module {
    @Getter
    protected float maxHp;
    @Getter
    @Setter
    protected float hp;
    @Getter
    protected Fixture fixture;
    private EventBus eventBus;
    @Getter
    protected Ship ship;
    @Getter
    private final EventBus moduleEventBus = new EventBus();

    protected DamageableModule(float hp) {
        this.maxHp = this.hp = hp;
    }

    protected DamageableModule(float hp, float sizeX, float sizeY) {
        super(sizeX, sizeY);
        this.maxHp = this.hp = hp;
    }

    public void init(Ship ship) {
        this.ship = ship;
        this.eventBus = ship.getWorld().getEventBus();
        createFixture(ship);
    }

    protected abstract void createFixture(RigidBody rigidBody);

    public void addFixtureToBody(RigidBody rigidBody) {
        if (isDead) {
            return;
        }

        rigidBody.addFixture(fixture);
    }

    public boolean damage(float amount) {
        if (hp > 0) {
            hp -= amount;

            if (hp <= 0) {
                setDead();
            }
        }

        return true;
    }

    @Override
    public void setDead() {
        if (!isDead) {
            destroy();
        }

        super.setDead();
    }

    protected void destroy() {
        ModuleDestroyEvent event = new ModuleDestroyEvent(this);
        eventBus.publish(event);
        moduleEventBus.publish(event);
    }
}