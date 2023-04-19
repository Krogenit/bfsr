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
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

public class ShipHumanSmall0 extends Ship {
    public ShipHumanSmall0() {
        super(6.9423f, 6.9423f, TextureRegister.shipHumanSmall0.ordinal());
    }

    @Override
    public void init(WorldServer world) {
        super.init(world);
        setEngine(new Engine(1.2f, 1.0f, 1.0f, 30.0f, 0.99f, 2.5f));

        setReactor(new Reactor(30.0f, 9.0f));

        setHull(new Hull(25.0f, 0.025f));

        Armor armor = new Armor();
        armor.setArmorPlateByDir(Direction.FORWARD, new ArmorPlate(25.0f, 0.45f, 1.15f));
        armor.setArmorPlateByDir(Direction.BACKWARD, new ArmorPlate(25.0f, 0.45f, 1.15f));
        armor.setArmorPlateByDir(Direction.LEFT, new ArmorPlate(25.0f, 0.45f, 1.15f));
        armor.setArmorPlateByDir(Direction.RIGHT, new ArmorPlate(25.0f, 0.45f, 1.15f));
        setArmor(armor);

        ShieldConfig shieldConfig = ShieldRegistry.INSTANCE.getShield("humanSmall0");
        Shield shield = new Shield(this, shieldConfig);
        setShield(shield);
        shield.createBody();

        setCrew(new Crew(2));

        setCargo(new Cargo(2));

        maxDestroyingTimer = 60;
        maxSparksTimer = 20;
    }

    @Override
    protected void initBody() {
        Vector2[] vertices = new Vector2[7];
        vertices[0] = new Vector2(-2.9f, 0.2f);
        vertices[1] = new Vector2(-2.9f, -0.2f);
        vertices[2] = new Vector2(-1.0f, -3.1f);
        vertices[3] = new Vector2(0.6f, -3.1f);
        vertices[4] = new Vector2(2.7f, 0.1f);
        vertices[5] = new Vector2(0.6f, 3.1f);
        vertices[6] = new Vector2(-1.0f, 3.1f);

        PathD pathD = new PathD(vertices.length);
        for (int i = 0; i < vertices.length; i++) {
            Vector2 vector2 = vertices[i];
            pathD.add(new PointD(vector2.x, vector2.y));
        }
        contours.add(pathD);

        mask = new DamageMask(128, 128);

        BodyFixture fixture = new BodyFixture(new Polygon(vertices));
        setupFixture(fixture);
        body.addFixture(fixture);
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
    protected Vector2f getWeaponSlotPosition(int id) {
        if (id == 0) return new Vector2f(0.7f, 2.4f);
        else if (id == 1) return new Vector2f(0.7f, -2.4f);
        throw new UnsupportedOperationException("Unsupported weapon slot position " + id);
    }

    @Override
    public ShipType getType() {
        return ShipType.HUMAN_SMALL_0;
    }
}