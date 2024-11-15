package net.bfsr.entity.ship;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.component.armor.ArmorPlateRegistry;
import net.bfsr.config.component.cargo.CargoRegistry;
import net.bfsr.config.component.crew.CrewRegistry;
import net.bfsr.config.component.engine.EngineRegistry;
import net.bfsr.config.component.hull.HullRegistry;
import net.bfsr.config.component.reactor.ReactorRegistry;
import net.bfsr.config.component.shield.ShieldRegistry;
import net.bfsr.config.component.weapon.beam.BeamRegistry;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.cargo.Cargo;
import net.bfsr.entity.ship.module.crew.Crew;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponFactory;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.faction.Faction;
import net.bfsr.logic.LogicType;
import net.bfsr.math.Direction;

import java.util.EnumMap;

public class ShipOutfitter {
    private final EngineRegistry engineRegistry;
    private final ReactorRegistry reactorRegistry;
    private final HullRegistry hullRegistry;
    private final ArmorPlateRegistry armorPlateRegistry;
    private final ShieldRegistry shieldRegistry;
    private final CrewRegistry crewRegistry;
    private final CargoRegistry cargoRegistry;
    private final GunRegistry gunRegistry;
    private final BeamRegistry beamRegistry;

    public ShipOutfitter(ConfigConverterManager configConverterManager) {
        this.engineRegistry = configConverterManager.getConverter(EngineRegistry.class);
        this.reactorRegistry = configConverterManager.getConverter(ReactorRegistry.class);
        this.hullRegistry = configConverterManager.getConverter(HullRegistry.class);
        this.armorPlateRegistry = configConverterManager.getConverter(ArmorPlateRegistry.class);
        this.shieldRegistry = configConverterManager.getConverter(ShieldRegistry.class);
        this.crewRegistry = configConverterManager.getConverter(CrewRegistry.class);
        this.cargoRegistry = configConverterManager.getConverter(CargoRegistry.class);
        this.gunRegistry = configConverterManager.getConverter(GunRegistry.class);
        this.beamRegistry = configConverterManager.getConverter(BeamRegistry.class);
    }

    public void outfit(Ship ship) {
        if (ship.getFaction() == Faction.HUMAN) {
            outfitHuman(ship);
        } else if (ship.getFaction() == Faction.SAIMON) {
            outfitSaimon(ship);
        } else if (ship.getFaction() == Faction.ENGI) {
            outfitEngi(ship);
        }
    }

    private void outfitHuman(Ship ship) {
        outfit(ship, "human");
    }

    private void outfitSaimon(Ship ship) {
        outfit(ship, "saimon");
    }

    private void outfitEngi(Ship ship) {
        outfit(ship, "engi");
    }

    private void outfit(Ship ship, String factionName) {
        ship.setEngine(new Engines(engineRegistry.get(factionName), ship));
        ship.setReactor(new Reactor(reactorRegistry.get(factionName), ship.getShipData().getReactorPolygon()));
        ship.setHull(new Hull(hullRegistry.get(factionName), ship));
        ship.setArmor(new Armor(armorPlateRegistry.get(factionName), ship));
        ship.setShield(new Shield(shieldRegistry.get(factionName), ship.getShipData().getShieldPolygon(),
                ship.getWorld().getGameLogic().getLogic(LogicType.SHIELD_UPDATE.ordinal())));
        ship.setCrew(new Crew(crewRegistry.get(factionName)));
        ship.setCargo(new Cargo(cargoRegistry.get(factionName)));
        addWeapons(ship);
    }

    public void outfit(Ship ship, int reactorDataId, int enginesDataId, EnumMap<Direction, BooleanArrayList> enginesMap,
                       int hullDataId, int armorDataId, int crewDataId, int cargoDataId, int shieldDataId) {
        ship.setReactor(new Reactor(reactorRegistry.get(reactorDataId),
                ship.getShipData().getReactorPolygon()));
        Engines engines = new Engines(engineRegistry.get(enginesDataId), ship);
        ship.setEngine(engines);

        ship.setHull(new Hull(hullRegistry.get(hullDataId), ship));
        ship.setArmor(new Armor(armorPlateRegistry.get(armorDataId), ship));
        ship.setCrew(new Crew(crewRegistry.get(crewDataId)));
        ship.setCargo(new Cargo(cargoRegistry.get(cargoDataId)));
        if (shieldDataId > -1) {
            Shield shield = new Shield(shieldRegistry.get(shieldDataId), ship.getShipData().getShieldPolygon(),
                    ship.getWorld().getGameLogic().getLogic(LogicType.SHIELD_UPDATE.ordinal()));
            ship.setShield(shield);
        }
    }

    private void addWeapons(Ship ship) {
        if (ship.getFaction() == Faction.HUMAN) {
            addRandomWeapons(ship, "plasm_small");
        } else if (ship.getFaction() == Faction.SAIMON) {
            addRandomWeapons(ship, "laser_small");
        } else {
            addRandomWeapons(ship, "gaus_small");
        }
    }

    private void addRandomWeapons(Ship ship, String gunName) {
        if (ship.getWorld().getRand().nextInt(3) == 0) {
            initAndAddWeaponToShip(ship, WeaponFactory.createBeam("beam_small", beamRegistry), 0);
            initAndAddWeaponToShip(ship, WeaponFactory.createBeam("beam_small", beamRegistry), 1);
        } else {
            initAndAddWeaponToShip(ship, WeaponFactory.createGun(gunName, gunRegistry), 0);
            initAndAddWeaponToShip(ship, WeaponFactory.createGun(gunName, gunRegistry), 1);
        }
    }

    private void initAndAddWeaponToShip(Ship ship, WeaponSlot weaponSlot, int id) {
        weaponSlot.init(id, ship);
        ship.addWeaponToSlot(id, weaponSlot);
    }
}