package net.bfsr.entity.ship;

import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom;
import net.bfsr.config.component.armor.ArmorPlateRegistry;
import net.bfsr.config.component.cargo.CargoRegistry;
import net.bfsr.config.component.crew.CrewRegistry;
import net.bfsr.config.component.engine.EngineRegistry;
import net.bfsr.config.component.hull.HullRegistry;
import net.bfsr.config.component.reactor.ReactorRegistry;
import net.bfsr.config.component.shield.ShieldData;
import net.bfsr.config.component.shield.ShieldRegistry;
import net.bfsr.config.component.weapon.beam.BeamRegistry;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.engine.config.ConfigConverterManager;
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
import org.joml.Vector2f;

import java.util.Locale;

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

    private final XoRoShiRo128PlusPlusRandom random = new XoRoShiRo128PlusPlusRandom();

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
        outfit(ship, getShipFactionString(ship));
    }

    private void outfit(Ship ship, String factionName) {
        if (ship.getConfigData().getFileName().equals("engi_huge0")) {
            ship.setEngine(new Engines(engineRegistry.get("engi_huge0"), ship));
            ship.setReactor(new Reactor(reactorRegistry.get("engi_huge0"), ship.getConfigData().getReactorPolygon()));
            ship.setHull(new Hull(hullRegistry.get(factionName), ship));
            ship.setArmor(new Armor(armorPlateRegistry.get(factionName), ship));
            addShieldToShip(ship, "engi_huge0");
            ship.setCrew(new Crew(crewRegistry.get(factionName)));
            ship.setCargo(new Cargo(cargoRegistry.get(factionName)));
        } else {
            ship.setEngine(new Engines(engineRegistry.get(factionName), ship));
            ship.setReactor(new Reactor(reactorRegistry.get(factionName), ship.getConfigData().getReactorPolygon()));
            ship.setHull(new Hull(hullRegistry.get(factionName), ship));
            ship.setArmor(new Armor(armorPlateRegistry.get(factionName), ship));
            addShieldToShip(ship, factionName);
            ship.setCrew(new Crew(crewRegistry.get(factionName)));
            ship.setCargo(new Cargo(cargoRegistry.get(factionName)));
        }

        addWeapons(ship);
    }

    public void outfit(Ship ship, int reactorDataId, int enginesDataId, int hullDataId, int armorDataId, int crewDataId, int cargoDataId,
                       int shieldDataId) {
        ship.setReactor(new Reactor(reactorRegistry.get(reactorDataId),
                ship.getConfigData().getReactorPolygon()));
        Engines engines = new Engines(engineRegistry.get(enginesDataId), ship);
        ship.setEngine(engines);

        ship.setHull(new Hull(hullRegistry.get(hullDataId), ship));
        ship.setArmor(new Armor(armorPlateRegistry.get(armorDataId), ship));
        ship.setCrew(new Crew(crewRegistry.get(crewDataId)));
        ship.setCargo(new Cargo(cargoRegistry.get(cargoDataId)));
        if (shieldDataId > -1) {
            Shield shield = new Shield(shieldRegistry.get(shieldDataId), ship.getConfigData().getShieldPolygon(),
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
        if (random.nextInt(3) == 0) {
            addBeamGuns(ship);
        } else {
            addProjectileGuns(ship, gunName);
        }
    }

    public void addBeamGuns(Ship ship) {
        Vector2f[] weaponSlotPositions = ship.getConfigData().getWeaponSlotPositions();
        for (int i = 0; i < weaponSlotPositions.length; i++) {
            initAndAddWeaponToShip(ship, WeaponFactory.createBeam("beam_small", beamRegistry), i);
        }
    }

    public void addProjectileGuns(Ship ship, String gunName) {
        Vector2f[] weaponSlotPositions = ship.getConfigData().getWeaponSlotPositions();
        for (int i = 0; i < weaponSlotPositions.length; i++) {
            initAndAddWeaponToShip(ship, WeaponFactory.createGun(gunName, gunRegistry), i);
        }
    }

    private void initAndAddWeaponToShip(Ship ship, WeaponSlot weaponSlot, int id) {
        weaponSlot.init(id, ship);
        ship.addWeaponToSlot(id, weaponSlot);
    }

    public void addShieldToShip(Ship ship) {
        addShieldToShip(ship, getShipFactionString(ship));
    }

    public void addShieldToShip(Ship ship, String shieldData) {
        addShieldToShip(ship, shieldRegistry.get(shieldData));
    }

    public void addShieldToShip(Ship ship, int id) {
        addShieldToShip(ship, shieldRegistry.get(id));
    }

    public void addShieldToShip(Ship ship, ShieldData shieldData) {
        ship.setShield(new Shield(shieldData, ship.getConfigData().getShieldPolygon(),
                ship.getWorld().getGameLogic().getLogic(LogicType.SHIELD_UPDATE.ordinal())));
    }

    private String getShipFactionString(Ship ship) {
        return ship.getFaction().name().toLowerCase(Locale.getDefault());
    }
}