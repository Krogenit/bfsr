package net.bfsr.entity.wreck;

import clipper2.core.PathsD;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.Damageable;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.event.EventBus;
import net.bfsr.event.entity.wreck.BulletDamageShipWreckEvent;
import net.bfsr.event.entity.wreck.ShipWreckDeathEvent;
import net.bfsr.event.entity.wreck.ShipWreckFixturesEvent;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.ShipWreckFilter;
import org.dyn4j.dynamics.BodyFixture;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ShipWreck extends RigidBody implements Damageable {
    @Getter
    private final DamageMask mask;
    @Getter
    @Setter
    private PathsD contours;
    @Getter
    private final List<BodyFixture> fixturesToAdd = new ArrayList<>();
    private final int maxLifeTime = 1200;
    @Getter
    private int dataIndex;

    public ShipWreck(float x, float y, float sin, float cos, float scaleX, float scaleY, int dataIndex,
                     DamageMask mask, PathsD contours) {
        super(x, y, sin, cos, scaleX, scaleY);
        this.dataIndex = dataIndex;
        this.mask = mask;
        this.contours = contours;
    }

    @Override
    public BodyFixture setupFixture(BodyFixture bodyFixture) {
        bodyFixture.setFilter(new ShipWreckFilter(this));
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        return bodyFixture;
    }

    @Override
    public void update() {
        if (lifeTime++ >= maxLifeTime) {
            setDead();
        }
    }

    public void bulletDamage(Bullet bullet, float contactX, float contactY, float normalX, float normalY) {
        EventBus.post(world.getSide(), new BulletDamageShipWreckEvent(this, bullet, contactX, contactY, normalX, normalY));
    }

    @Override
    public void setFixtures(List<BodyFixture> fixtures) {
        Damageable.super.setFixtures(fixtures);
        EventBus.post(world.getSide(), new ShipWreckFixturesEvent(this));
    }

    @Override
    public void setDead() {
        super.setDead();
        EventBus.post(world.getSide(), new ShipWreckDeathEvent(this));
    }

    @Override
    public float getSin() {
        return (float) body.getTransform().getSint();
    }

    @Override
    public float getCos() {
        return (float) body.getTransform().getCost();
    }
}