package net.bfsr.server.entity.ship;

import net.bfsr.collision.filter.ShipFilter;
import net.bfsr.component.Armor;
import net.bfsr.component.ArmorPlate;
import net.bfsr.component.Engine;
import net.bfsr.component.cargo.Cargo;
import net.bfsr.component.crew.Crew;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.reactor.Reactor;
import net.bfsr.component.shield.ShieldRegistry;
import net.bfsr.config.component.ShieldConfig;
import net.bfsr.entity.ship.ShipType;
import net.bfsr.math.Direction;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.server.component.Shield;
import net.bfsr.server.entity.Ship;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

public class ShipSaimonSmall0 extends Ship {
    public ShipSaimonSmall0(WorldServer world, float x, float y, float rot, boolean spawned) {
        super(world, x, y, rot, 10, 10, 1.0f, 0.6f, 0.5f, spawned);
    }

    @Override
    public void init() {
        setEngine(new Engine(1.2f, 1.0f, 1.0f, 30.0f, 0.99f, 2.6f));

        setReactor(new Reactor(30.0f, 9.75f));

        setHull(new Hull(25.0f, 0.025f, this));

        Armor armor = new Armor();
        armor.setArmorPlateByDir(Direction.FORWARD, new ArmorPlate(20.0f, 0.3f, 1.1f));
        armor.setArmorPlateByDir(Direction.BACKWARD, new ArmorPlate(20.0f, 0.3f, 1.1f));
        armor.setArmorPlateByDir(Direction.LEFT, new ArmorPlate(20.0f, 0.3f, 1.1f));
        armor.setArmorPlateByDir(Direction.RIGHT, new ArmorPlate(20.0f, 0.3f, 1.1f));
        setArmor(armor);

        ShieldConfig shieldConfig = ShieldRegistry.INSTANCE.getShield("saimonSmall0");
        Shield shield = new Shield(this, shieldConfig);
        setShield(shield);
        shield.createBody();

        setWeaponsCount(2);

        setCrew(new Crew(2));

        setCargo(new Cargo(2));

        createWeaponPosition(new Vector2f(1.5f, 2.3f));
        createWeaponPosition(new Vector2f(1.5f, -2.3f));

        maxDestroyingTimer = 60;
        maxSparksTimer = 20;
    }

    @Override
    protected void createBody(float x, float y) {
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-5.05f, -1.75f);
        vertices[1] = new Vector2(-3.45f, -1.75f);
        vertices[2] = new Vector2(-0.57f, -0.95f);
        vertices[3] = new Vector2(-1.5f, -0.0f);
        BodyFixture fixture = new BodyFixture(new Polygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        body.addFixture(fixture);

        vertices = new Vector2[4];
        vertices[0] = new Vector2(-1.5f, 0.0f);
        vertices[1] = new Vector2(-0.57f, 0.85f);
        vertices[2] = new Vector2(-3.1f, 1.65f);
        vertices[3] = new Vector2(-5.05f, 1.65f);
        fixture = new BodyFixture(new Polygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        body.addFixture(fixture);

        vertices = new Vector2[6];
        vertices[0] = new Vector2(-3.3f, -0.9f);
        vertices[1] = new Vector2(4.0f, -0.9f);
        vertices[2] = new Vector2(5.06f, -0.05f);
        vertices[3] = new Vector2(4.0f, 0.74f);
        vertices[4] = new Vector2(-3.3f, 0.74f);
        vertices[5] = new Vector2(-3.9f, -0.05f);
        fixture = new BodyFixture(new Polygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        body.addFixture(fixture);

        vertices = new Vector2[4];
        vertices[0] = new Vector2(-0.4f, -0.9f);
        vertices[1] = new Vector2(-0.9f, -3.1f);
        vertices[2] = new Vector2(1.4f, -3.1f);
        vertices[3] = new Vector2(1.4f, -0.9f);
        fixture = new BodyFixture(new Polygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        body.addFixture(fixture);

        vertices = new Vector2[4];
        vertices[0] = new Vector2(-0.9f, 3.05f);
        vertices[1] = new Vector2(-0.4f, 0.75f);
        vertices[2] = new Vector2(1.4f, 0.75f);
        vertices[3] = new Vector2(1.4f, 3.05f);
        fixture = new BodyFixture(new Polygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        body.addFixture(fixture);


        recalculateMass();
        body.translate(x, y);
        body.setUserData(this);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    @Override
    protected void createDestroyParticles() {
        WreckSpawner.spawnDestroyShipSmall(this);
    }

    @Override
    public ShipType getType() {
        return ShipType.SAIMON_SMALL_0;
    }
}
