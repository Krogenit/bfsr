package net.bfsr.entity.ship;

import lombok.Getter;
import net.bfsr.ai.Ai;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.DamageMask;
import net.bfsr.engine.math.LUT;
import net.bfsr.faction.Faction;
import net.bfsr.world.World;

@Getter
public class ShipFactory {
    private final ShipRegistry shipRegistry;
    private final ShipOutfitter shipOutfitter;

    public ShipFactory(ShipRegistry shipRegistry, ShipOutfitter shipOutfitter) {
        this.shipRegistry = shipRegistry;
        this.shipOutfitter = shipOutfitter;
    }

    public Ship createPlayerShipHumanSmall(World world, float x, float y, float angle) {
        return create(world, world.getNextId(), x, y, angle, Faction.HUMAN, shipRegistry.get("human_small"));
    }

    public Ship createPlayerShipSaimonSmall(World world, float x, float y, float angle) {
        return create(world, world.getNextId(), x, y, angle, Faction.SAIMON, shipRegistry.get("saimon_small"));
    }

    public Ship createPlayerShipEngiSmall(World world, float x, float y, float angle) {
        return create(world, world.getNextId(), x, y, angle, Faction.ENGI, shipRegistry.get("engi_small"));
    }

    public Ship createBotHumanSmall(World world, float x, float y, float angle, Ai ai) {
        return createBot(world, x, y, angle, Faction.HUMAN, shipRegistry.get("human_small"), ai);
    }

    public Ship createBotSaimonSmall(World world, float x, float y, float angle, Ai ai) {
        return createBot(world, x, y, angle, Faction.SAIMON, shipRegistry.get("saimon_small"), ai);
    }

    public Ship createBotEngiSmall(World world, float x, float y, float angle, Ai ai) {
        return createBot(world, x, y, angle, Faction.ENGI, shipRegistry.get("engi_small"), ai);
    }

    private Ship create(World world, int id, float x, float y, float angle, Faction faction, ShipData shipData) {
        Ship ship = create(x, y, LUT.sin(angle), LUT.cos(angle), faction, shipData, new DamageMask(32, 32));
        ship.init(world, id);
        return ship;
    }

    public Ship create(float x, float y, float sin, float cos, Faction faction, ShipData shipData, DamageMask damageMask) {
        Ship ship = create(faction, shipData, damageMask);
        ship.setPosition(x, y);
        ship.setRotation(sin, cos);
        return ship;
    }

    public Ship createBot(World world, float x, float y, float angle, Faction faction, ShipData shipData, Ai ai) {
        Ship ship = create(world, world.getNextId(), x, y, angle, faction, shipData);
        ship.init(world, world.getNextId());
        ship.setName("[BOT] " + ship.getFaction().toString());
        shipOutfitter.outfit(ship);
        ai.init(ship);
        ship.setAi(ai);
        return ship;
    }

    private Ship create(Faction faction, ShipData shipData, DamageMask damageMask) {
        Ship ship = new Ship(shipData, damageMask);
        ship.setFaction(faction);
        return ship;
    }
}