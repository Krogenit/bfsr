package net.bfsr.server.entity.ship;

import clipper2.core.PathD;
import clipper2.core.PointD;
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
import net.bfsr.server.component.Shield;
import net.bfsr.server.damage.DamageMask;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.world.WorldServer;
import net.bfsr.texture.TextureRegister;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.decompose.SweepLine;
import org.joml.Vector2f;

import java.util.List;

public class ShipSaimonSmall0 extends Ship {
    public ShipSaimonSmall0(WorldServer world, float x, float y, float rotation, boolean spawned) {
        super(world, x, y, rotation, 12.8f, 12.8f, spawned, TextureRegister.shipSaimonSmall0.ordinal());
    }

    @Override
    public void init() {
        super.init();
        setEngine(new Engine(1.2f, 1.0f, 1.0f, 30.0f, 0.99f, 2.6f));

        setReactor(new Reactor(30.0f, 9.75f));

        setHull(new Hull(25.0f, 0.025f));

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
    protected void initBody() {
        Vector2[] vertices = new Vector2[21];

        vertices[0] = new Vector2(-3.83f, -0.0f);
        vertices[1] = new Vector2(-3.23f, -1.33f);
        vertices[2] = new Vector2(-4.90f, -1.8f);
        vertices[3] = new Vector2(-3.17f, -1.77f);
        vertices[4] = new Vector2(-0.33f, -0.93f);
        vertices[5] = new Vector2(-1.13f, -2.37f);
        vertices[6] = new Vector2(-1.03f, -3.20f);
        vertices[7] = new Vector2(1.35f, -3.20f);
        vertices[8] = new Vector2(1.75f, -1.0f);
        vertices[9] = new Vector2(3.63f, -1.0f);
        vertices[10] = new Vector2(4.9f, -0.30f);

        vertices[11] = new Vector2(4.9f, 0.22f);
        vertices[12] = new Vector2(3.63f, 0.92f);
        vertices[13] = new Vector2(1.75f, 0.92f);
        vertices[14] = new Vector2(1.35f, 3.16f);
        vertices[15] = new Vector2(-1.03f, 3.16f);
        vertices[16] = new Vector2(-1.13f, 2.37f);
        vertices[17] = new Vector2(-0.33f, 0.93f);
        vertices[18] = new Vector2(-3.17f, 1.65f);
        vertices[19] = new Vector2(-4.90f, 1.65f);
        vertices[20] = new Vector2(-3.23f, 1.28f);

        PathD pathD = new PathD(vertices.length);
        for (int i = 0; i < vertices.length; i++) {
            Vector2 vector2 = vertices[i];
            pathD.add(new PointD(vector2.x, vector2.y));
        }
        contours.add(pathD);

        mask = new DamageMask(128, 128);

        SweepLine sweepLine = new SweepLine();
        List<Convex> convexes = sweepLine.decompose(vertices);
        for (int i = 0; i < convexes.size(); i++) {
            Convex convex = convexes.get(i);
            BodyFixture fixture = new BodyFixture(convex);
            setupFixture(fixture);
            body.addFixture(fixture);
        }

        body.setMass(MassType.NORMAL);
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