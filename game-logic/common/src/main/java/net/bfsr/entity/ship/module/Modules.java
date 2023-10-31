package net.bfsr.entity.ship.module;

import lombok.Getter;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.cargo.Cargo;
import net.bfsr.entity.ship.module.crew.Crew;
import net.bfsr.entity.ship.module.engine.Engine;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;

import java.util.ArrayList;
import java.util.List;

public class Modules {
    private final List<Module> moduleList = new ArrayList<>();
    @Getter
    private Shield shield;
    @Getter
    private Engine engine;
    @Getter
    private Reactor reactor;
    @Getter
    private Armor armor;
    @Getter
    private Hull hull;
    @Getter
    private Crew crew;
    @Getter
    private Cargo cargo;
    @Getter
    private final List<WeaponSlot> weaponSlots = new ArrayList<>();

    public void update() {
        for (int i = 0; i < moduleList.size(); i++) {
            moduleList.get(i).update();
        }

        hull.repair(crew.getCrewRegen());

        for (int size = weaponSlots.size(), i = 0; i < size; i++) {
            weaponSlots.get(i).update();
        }
    }

    public void updateWeaponSlotPositions() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot != null) weaponSlot.updatePos();
        }
    }

    public void shoot() {
        for (int i = 0, size = weaponSlots.size(); i < size; i++) {
            weaponSlots.get(i).tryShoot();
        }
    }

    public void addWeaponToSlot(int id, WeaponSlot slot, Ship ship) {
        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot.getId() == id) {
                weaponSlot.clear();
                weaponSlots.set(i, slot);
                slot.init(id, ship);
                return;
            }
        }

        slot.init(id, ship);
        weaponSlots.add(slot);
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
        addModule(engine);
    }

    public void setShield(Shield shield) {
        this.shield = shield;
        addModule(shield);
    }

    public void setHull(Hull hull) {
        this.hull = hull;
        addModule(hull);
    }

    public void setCrew(Crew crew) {
        this.crew = crew;
        addModule(crew);
    }

    public void setReactor(Reactor reactor) {
        this.reactor = reactor;
        addModule(reactor);
    }

    public void setArmor(Armor armor) {
        this.armor = armor;
        addModule(armor);
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
        addModule(cargo);
    }

    private void addModule(Module module) {
        moduleList.removeIf(module1 -> module1.getType() == module.getType());
        moduleList.add(module);
    }

    public WeaponSlot getWeaponSlot(int i) {
        return weaponSlots.get(i);
    }

    public void clear() {
        int size = weaponSlots.size();
        for (int i = 0; i < size; i++) {
            WeaponSlot slot = weaponSlots.get(i);
            if (slot != null) slot.clear();
        }
    }
}