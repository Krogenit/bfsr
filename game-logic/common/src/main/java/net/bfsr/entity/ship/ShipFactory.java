package net.bfsr.entity.ship;

import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.engine.math.LUT;
import net.bfsr.faction.Faction;
import net.bfsr.world.World;

public class ShipFactory {
    private static final ShipFactory INSTANCE = new ShipFactory();

    public Ship createPlayerShipHumanSmall(World world, float x, float y, float angle) {
        return create(world, world.getNextId(), x, y, angle, Faction.HUMAN, ShipRegistry.INSTANCE.get("human_small"));
    }

    public Ship createPlayerShipSaimonSmall(World world, float x, float y, float angle) {
        return create(world, world.getNextId(), x, y, angle, Faction.SAIMON, ShipRegistry.INSTANCE.get("saimon_small"));
    }

    public Ship createPlayerShipEngiSmall(World world, float x, float y, float angle) {
        return create(world, world.getNextId(), x, y, angle, Faction.ENGI, ShipRegistry.INSTANCE.get("engi_small"));
    }

    public Ship createBotHumanSmall(World world, float x, float y, float angle) {
        return createBot(world, x, y, angle, Faction.HUMAN, ShipRegistry.INSTANCE.get("human_small"));
    }

    public Ship createBotSaimonSmall(World world, float x, float y, float angle) {
        return createBot(world, x, y, angle, Faction.SAIMON, ShipRegistry.INSTANCE.get("saimon_small"));
    }

    public Ship createBotEngiSmall(World world, float x, float y, float angle) {
        return createBot(world, x, y, angle, Faction.ENGI, ShipRegistry.INSTANCE.get("engi_small"));
    }

    private Ship create(World world, int id, float x, float y, float angle, Faction faction, ShipData shipData) {
        return create(world, id, x, y, LUT.sin(angle), LUT.cos(angle), faction, shipData);
    }

    public Ship create(World world, int id, float x, float y, float sin, float cos, Faction faction, ShipData shipData) {
        Ship ship = create(world, id, faction, shipData);
        ship.setPosition(x, y);
        ship.setRotation(sin, cos);
        return ship;
    }

    private Ship createBot(World world, float x, float y, float angle, Faction faction, ShipData shipData) {
        Ship ship = create(world, world.getNextId(), x, y, angle, faction, shipData);
        ship.setName("[BOT] " + ship.getFaction().toString());
        ShipOutfitter.get().outfit(ship);
        return ship;
    }

    private Ship create(World world, int id, Faction faction, ShipData shipData) {
        Ship ship = new Ship(shipData);
        ship.init(world, id);
        ship.setFaction(faction);
        return ship;
    }

    public static ShipFactory get() {
        return INSTANCE;
    }
}