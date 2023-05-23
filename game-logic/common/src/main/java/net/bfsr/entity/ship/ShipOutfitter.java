package net.bfsr.entity.ship;

import net.bfsr.component.armor.Armor;
import net.bfsr.component.cargo.Cargo;
import net.bfsr.component.crew.Crew;
import net.bfsr.component.engine.Engine;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.reactor.Reactor;
import net.bfsr.component.shield.Shield;
import net.bfsr.component.weapon.WeaponFactory;
import net.bfsr.config.component.armor.ArmorPlateRegistry;
import net.bfsr.config.component.cargo.CargoRegistry;
import net.bfsr.config.component.crew.CrewRegistry;
import net.bfsr.config.component.engine.EngineRegistry;
import net.bfsr.config.component.hull.HullRegistry;
import net.bfsr.config.component.reactor.ReactorRegistry;
import net.bfsr.config.component.shield.ShieldRegistry;
import net.bfsr.faction.Faction;

public class ShipOutfitter {
    private static final ShipOutfitter INSTANCE = new ShipOutfitter();

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
        ship.setEngine(new Engine(EngineRegistry.INSTANCE.get("human")));
        ship.setReactor(new Reactor(ReactorRegistry.INSTANCE.get("human")));
        ship.setHull(new Hull(HullRegistry.INSTANCE.get("human")));
        ship.setArmor(new Armor(ArmorPlateRegistry.INSTANCE.get("human")));
        ship.setShield(new Shield(ShieldRegistry.INSTANCE.get("human")));
        ship.setCrew(new Crew(CrewRegistry.INSTANCE.get("human")));
        ship.setCargo(new Cargo(CargoRegistry.INSTANCE.get("human")));
        addWeapons(ship);
    }

    private void outfitSaimon(Ship ship) {
        ship.setEngine(new Engine(EngineRegistry.INSTANCE.get("saimon")));
        ship.setReactor(new Reactor(ReactorRegistry.INSTANCE.get("saimon")));
        ship.setHull(new Hull(HullRegistry.INSTANCE.get("saimon")));
        ship.setArmor(new Armor(ArmorPlateRegistry.INSTANCE.get("saimon")));
        ship.setShield(new Shield(ShieldRegistry.INSTANCE.get("saimon")));
        ship.setCrew(new Crew(CrewRegistry.INSTANCE.get("saimon")));
        ship.setCargo(new Cargo(CargoRegistry.INSTANCE.get("saimon")));
        addWeapons(ship);
    }

    private void outfitEngi(Ship ship) {
        ship.setEngine(new Engine(EngineRegistry.INSTANCE.get("engi")));
        ship.setReactor(new Reactor(ReactorRegistry.INSTANCE.get("engi")));
        ship.setHull(new Hull(HullRegistry.INSTANCE.get("engi")));
        ship.setArmor(new Armor(ArmorPlateRegistry.INSTANCE.get("engi")));
        ship.setShield(new Shield(ShieldRegistry.INSTANCE.get("engi")));
        ship.setCrew(new Crew(CrewRegistry.INSTANCE.get("engi")));
        ship.setCargo(new Cargo(CargoRegistry.INSTANCE.get("engi")));
        addWeapons(ship);
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
            ship.addWeaponToSlot(0, WeaponFactory.createBeam("beam_small"));
            ship.addWeaponToSlot(1, WeaponFactory.createBeam("beam_small"));
        } else {
            ship.addWeaponToSlot(0, WeaponFactory.createGun(gunName));
            ship.addWeaponToSlot(1, WeaponFactory.createGun(gunName));
        }
    }

    public static ShipOutfitter get() {
        return INSTANCE;
    }
}