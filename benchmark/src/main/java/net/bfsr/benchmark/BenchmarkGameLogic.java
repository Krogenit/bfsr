package net.bfsr.benchmark;

import net.bfsr.ai.Ai;
import net.bfsr.client.Client;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.component.weapon.beam.BeamRegistry;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.config.entity.bullet.DamageConfigurable;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.network.packet.Packet;
import net.bfsr.world.World;

import java.util.List;
import java.util.Random;

public class BenchmarkGameLogic extends Client {
    private final Random random = new Random();
    private int pauseAfterTicks = 10;

    public BenchmarkGameLogic(Profiler profiler) {
        super(profiler);
    }

    @Override
    public void init() {
        super.init();
        createWorld(0);
        closeGui();
        getGuiManager().showHUD(new HUD());
        spawnObjects();
    }

    private void spawnObjects() {
        ConfigConverterManager configManager = getConfigConverterManager();
        ShipOutfitter shipOutfitter = new ShipOutfitter(configManager);
        ShipRegistry shipRegistry = configManager.getConverter(ShipRegistry.class);
        GunRegistry gunRegistry = configManager.getConverter(GunRegistry.class);
        BeamRegistry beamRegistry = configManager.getConverter(BeamRegistry.class);
        ShipFactory shipFactory = new ShipFactory(shipRegistry, shipOutfitter);
        World world = getWorld();

        int shipsCount = 1000;
        float offset = 14.0f;
        float rectangleSpawnHalfWidth = (float) (offset * Math.sqrt(shipsCount)) / 2;
        float rectangleSpawnHalfHeight = (float) (offset * Math.sqrt(shipsCount)) / 2;
        float x = -rectangleSpawnHalfWidth;
        float y = -rectangleSpawnHalfHeight;
        for (int i = 0; i < shipsCount; i++) {
            Ship ship = createRandomShip(x, y, world, shipFactory);

            ship.removeConnectedObject(ship.getWeaponSlot(0));
            ship.removeConnectedObject(ship.getWeaponSlot(1));
            if (i > shipsCount / 2) {
                WeaponSlot weaponSlot = new WeaponSlot(gunRegistry.get("plasm_small"));
                WeaponSlot weaponSlot1 = new WeaponSlot(gunRegistry.get("plasm_small"));
                weaponSlot.init(0, ship);
                weaponSlot1.init(1, ship);
                ship.addConnectedObject(weaponSlot);
                ship.addConnectedObject(weaponSlot1);
            } else {
                WeaponSlotBeam weaponSlotBeam = new WeaponSlotBeam(beamRegistry.get("beam_small"));
                WeaponSlotBeam weaponSlotBeam1 = new WeaponSlotBeam(beamRegistry.get("beam_small"));
                weaponSlotBeam.init(0, ship);
                weaponSlotBeam1.init(1, ship);
                ship.addConnectedObject(weaponSlotBeam);
                ship.addConnectedObject(weaponSlotBeam1);
            }

            world.add(ship, false);
            ship.setSpawned();
            x += offset;
            if (x >= rectangleSpawnHalfWidth) {
                y += offset;
                x = -rectangleSpawnHalfWidth;
            }
        }

        getParticleManager().clear();

        List<Ship> ships = world.getEntitiesByType(Ship.class);
        for (int i = 0; i < shipsCount; i++) {
            ships.get(i).shoot(weaponSlot -> {});
        }

        WreckRegistry wreckRegistry = configManager.getConverter(WreckRegistry.class);
        int wreckCount = 1000;
        offset = 10.0f;
        rectangleSpawnHalfWidth = (float) (offset * Math.sqrt(wreckCount)) / 2;
        rectangleSpawnHalfHeight = (float) (offset * Math.sqrt(wreckCount)) / 2;
        x = -rectangleSpawnHalfWidth;
        y = -rectangleSpawnHalfHeight;
        for (int i = 0; i < 1000; i++) {
            Wreck wreck = world.getObjectPools().getWrecksPool().get();
            wreck.init(world, world.getNextId(), 0, true, true, true, x, y, 0, 0, 0, 1, 0, 5, 5, 1200, WreckType.DEFAULT,
                    wreckRegistry.getWreck(WreckType.DEFAULT, 0));
            world.add(wreck);
            x += offset;
            if (x >= rectangleSpawnHalfWidth) {
                y += offset;
                x = -rectangleSpawnHalfWidth;
            }
        }

        int bulletsCount = 1000;
        offset = 4.0f;
        rectangleSpawnHalfWidth = (float) (offset * Math.sqrt(bulletsCount)) / 2;
        rectangleSpawnHalfHeight = (float) (offset * Math.sqrt(bulletsCount)) / 2;
        x = -rectangleSpawnHalfWidth;
        y = -rectangleSpawnHalfHeight;
        for (int i = 0; i < bulletsCount; i++) {
            Bullet bullet = new Bullet(x, y, 0, 1, gunRegistry.get("plasm_small"), ships.get(random.nextInt(ships.size())),
                    new BulletDamage(new DamageConfigurable(1, 1, 1)));
            bullet.init(world, world.getNextId());
            world.add(bullet);

            x += offset;
            if (x >= rectangleSpawnHalfWidth) {
                y += offset;
                x = -rectangleSpawnHalfWidth;
            }
        }
    }

    private Ship createRandomShip(float x, float y, World world, ShipFactory shipFactory) {
        return switch (random.nextInt(3)) {
            case 1 -> shipFactory.createBotSaimonSmall(world, x, y, 0.0f, Ai.NO_AI);
            case 2 -> shipFactory.createBotEngiSmall(world, x, y, 0.0f, Ai.NO_AI);
            default -> shipFactory.createBotHumanSmall(world, x, y, 0.0f, Ai.NO_AI);
        };
    }

    @Override
    public void update(double time) {
        super.update(time);
        if (pauseAfterTicks > 0) {
            pauseAfterTicks--;
            if (pauseAfterTicks == 0) {
                setPaused(true);
            }
        }
    }

    @Override
    public void sendUDPPacket(Packet packet) {}

    @Override
    public void sendTCPPacket(Packet packet) {}
}
