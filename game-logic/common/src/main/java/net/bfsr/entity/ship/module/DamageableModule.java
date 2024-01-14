package net.bfsr.entity.ship.module;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.event.EventBus;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.listener.EventListener;
import net.bfsr.event.module.ModuleDestroyEvent;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public abstract class DamageableModule extends Module implements net.bfsr.event.EventBus {
    @Getter
    protected float maxHp;
    @Getter
    @Setter
    protected float hp;
    @Getter
    protected BodyFixture fixture;
    private EventBus eventBus;
    @Getter
    protected Ship ship;
    private final List<EventListener> destroyListeners = new ArrayList<>();

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

    protected void createFixture(RigidBody<?> rigidBody) {}

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
        for (int i = 0; i < destroyListeners.size(); i++) {
            destroyListeners.get(i).event();
        }
    }

    public void addDestroyListener(EventListener listener) {
        destroyListeners.add(listener);
    }

    @Override
    public void removeListener(EventListener listener) {
        destroyListeners.remove(listener);
    }
}