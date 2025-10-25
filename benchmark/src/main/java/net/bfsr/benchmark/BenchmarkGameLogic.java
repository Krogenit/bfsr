package net.bfsr.benchmark;

import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom;
import net.bfsr.client.Client;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.config.component.weapon.beam.BeamRegistry;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.config.entity.bullet.DamageConfigurable;
import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.engine.ai.Ai;
import net.bfsr.engine.config.ConfigConverterManager;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.loop.AbstractGameLoop;
import net.bfsr.engine.network.packet.Packet;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.engine.world.World;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.entity.wreck.WreckType;

import java.util.List;

public class BenchmarkGameLogic extends Client {
    private final XoRoShiRo128PlusPlusRandom random = new XoRoShiRo128PlusPlusRandom();
    private int pauseAfterFrames = 10;

    public BenchmarkGameLogic(AbstractGameLoop gameLoop, Profiler profiler, EventBus eventBus) {
        super(gameLoop, profiler, eventBus);
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
        ShipFactory shipFactory = getShipFactory();
        ConfigConverterManager configManager = getConfigConverterManager();
        GunRegistry gunRegistry = configManager.getConverter(GunRegistry.class);
        BeamRegistry beamRegistry = configManager.getConverter(BeamRegistry.class);
        World world = getWorld();

        int shipsCount = 1000;
        float offset = 1.4f;
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
        offset = 1.0f;
        rectangleSpawnHalfWidth = (float) (offset * Math.sqrt(wreckCount)) / 2;
        rectangleSpawnHalfHeight = (float) (offset * Math.sqrt(wreckCount)) / 2;
        x = -rectangleSpawnHalfWidth;
        y = -rectangleSpawnHalfHeight;
        ObjectPool<Wreck> wreckPool = getObjectPool(Wreck.class);
        for (int i = 0; i < wreckCount; i++) {
            Wreck wreck = wreckPool.get();
            wreck.init(world, world.getNextId(), 0, true, true, true, x, y, 0, 0, 0, 1, 0, 0.5f, 0.5f, 1200, WreckType.DEFAULT,
                    wreckRegistry.getWreck(WreckType.DEFAULT, 0));
            world.add(wreck);
            x += offset;
            if (x >= rectangleSpawnHalfWidth) {
                y += offset;
                x = -rectangleSpawnHalfWidth;
            }
        }

        int bulletsCount = 1000;
        offset = 0.4f;
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
    public void update(int frame, double time) {
        super.update(frame, time);
        if (pauseAfterFrames > 0) {
            pauseAfterFrames--;
            if (pauseAfterFrames == 0) {
                setPaused(true);
            }
        }
    }

    @Override
    public void sendUDPPacket(Packet packet) {}

    @Override
    public void sendTCPPacket(Packet packet) {}
}
