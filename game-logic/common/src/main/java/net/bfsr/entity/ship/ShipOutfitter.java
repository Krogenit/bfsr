package net.bfsr.entity.ship;

import net.bfsr.config.component.armor.ArmorPlateRegistry;
import net.bfsr.config.component.cargo.CargoRegistry;
import net.bfsr.config.component.crew.CrewRegistry;
import net.bfsr.config.component.engine.EngineRegistry;
import net.bfsr.config.component.hull.HullRegistry;
import net.bfsr.config.component.reactor.ReactorRegistry;
import net.bfsr.config.component.shield.ShieldRegistry;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.cargo.Cargo;
import net.bfsr.entity.ship.module.crew.Crew;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponFactory;
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
        outfit(ship, "human");
    }

    private void outfitSaimon(Ship ship) {
        outfit(ship, "saimon");
    }

    private void outfitEngi(Ship ship) {
        outfit(ship, "engi");
    }

    private void outfit(Ship ship, String factionName) {
        ship.setEngine(new Engines(EngineRegistry.INSTANCE.get(factionName), ship));
        ship.setReactor(new Reactor(ReactorRegistry.INSTANCE.get(factionName), ship));
        ship.setHull(new Hull(HullRegistry.INSTANCE.get(factionName), ship));
        ship.setArmor(new Armor(ArmorPlateRegistry.INSTANCE.get(factionName), ship));
        ship.setShield(new Shield(ShieldRegistry.INSTANCE.get(factionName), ship));
        ship.setCrew(new Crew(CrewRegistry.INSTANCE.get(factionName)));
        ship.setCargo(new Cargo(CargoRegistry.INSTANCE.get(factionName)));
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