package net.bfsr.server.entity.ship;

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
import net.bfsr.server.collision.filter.ShipFilter;
import net.bfsr.server.component.Shield;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

public class ShipEngiSmall0 extends Ship {
    public ShipEngiSmall0(WorldServer world, float x, float y, float rot, boolean spawned) {
        super(world, x, y, rot, 7.5f, 7.5f, spawned);
    }

    @Override
    public void init() {
        setEngine(new Engine(1.2f, 1.0f, 1.0f, 30.0f, 0.99f, 2.5f));
        setReactor(new Reactor(30.0f, 9.0f));
        setHull(new Hull(22.5f, 0.0225f));

        Armor armor = new Armor();
        armor.setArmorPlateByDir(Direction.FORWARD, new ArmorPlate(17.5f, 0.25f, 1.05f));
        armor.setArmorPlateByDir(Direction.BACKWARD, new ArmorPlate(17.5f, 0.25f, 1.05f));
        armor.setArmorPlateByDir(Direction.LEFT, new ArmorPlate(17.5f, 0.25f, 1.05f));
        armor.setArmorPlateByDir(Direction.RIGHT, new ArmorPlate(17.5f, 0.25f, 1.05f));
        setArmor(armor);
        ShieldConfig shieldConfig = ShieldRegistry.INSTANCE.getShield("engiSmall0");
        Shield shield = new Shield(this, shieldConfig);
        setShield(shield);
        shield.createBody();
        setWeaponsCount(2);
        setCrew(new Crew(2));
        setCargo(new Cargo(2));
        createWeaponPosition(new Vector2f(1.8f, 1.8f));
        createWeaponPosition(new Vector2f(1.8f, -1.8f));

        maxDestroyingTimer = 60;
        maxSparksTimer = 20;
    }

    @Override
    protected void createBody(float x, float y) {
        Vector2[] vertices = new Vector2[7];
        vertices[0] = new Vector2(-3.6f, 0.0f);
        vertices[1] = new Vector2(-1.7f, -2.0f);
        vertices[2] = new Vector2(1.0f, -2.0f);
        vertices[3] = new Vector2(3.6f, -0.55f);
        vertices[4] = new Vector2(3.6f, 0.55f);
        vertices[5] = new Vector2(1.0f, 2.0f);
        vertices[6] = new Vector2(-1.7f, 2.0f);
        BodyFixture fixture = new BodyFixture(new Polygon(vertices));
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
        return ShipType.ENGI_SMALL_0;
    }
}
