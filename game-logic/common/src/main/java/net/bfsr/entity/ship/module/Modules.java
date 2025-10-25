package net.bfsr.entity.ship.module;

import lombok.Getter;
import net.bfsr.engine.event.EventBus;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.cargo.Cargo;
import net.bfsr.entity.ship.module.crew.Crew;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.event.module.ModuleAddEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

public class Modules {
    private final List<Module> moduleList = new ArrayList<>();
    private final EnumMap<ModuleType, List<Module>> modulesByType = new EnumMap<>(ModuleType.class);
    @Getter
    private Shield shield;
    @Getter
    private Engines engines;
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
    private Ship ship;
    private EventBus eventBus;

    public void init(Ship ship) {
        this.ship = ship;
        this.eventBus = ship.getWorld().getEventBus();
    }

    public void update() {
        for (int i = 0; i < moduleList.size(); i++) {
            moduleList.get(i).update();
        }

        hull.repair(crew.getCrewRegen());
    }

    public void shoot(Consumer<WeaponSlot> onShotEvent) {
        for (int i = 0, size = weaponSlots.size(); i < size; i++) {
            weaponSlots.get(i).tryShoot(onShotEvent, reactor);
        }
    }

    public void addWeaponToSlot(int id, WeaponSlot slot) {
        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot.getId() == id) {
                weaponSlot.removeFixture();
                ship.getConnectedObjects().remove(weaponSlot);
                weaponSlots.set(i, slot);
                modulesByType.get(ModuleType.WEAPON_SLOT).set(i, slot);
                return;
            }
        }

        weaponSlots.add(slot);
        modulesByType.computeIfAbsent(ModuleType.WEAPON_SLOT, moduleType -> new ArrayList<>(2)).add(slot);
    }

    public void setEngines(Engines engines) {
        engines.init(ship);
        this.engines = engines;
        addModule(engines);
    }

    public void setShield(Shield shield) {
        shield.init(ship);
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
        reactor.init(ship);
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
        for (int i = 0; i < moduleList.size(); i++) {
            Module module1 = moduleList.get(i);
            if (module1.getType() == module.getType()) {
                throw new RuntimeException("Module with type " + module.getType() + " already exists");
            }
        }

        moduleList.add(module);
        module.addToList(modulesByType.computeIfAbsent(module.getType(), moduleType -> new ArrayList<>(1)));
        ModuleAddEvent event = new ModuleAddEvent(ship, module);
        eventBus.publish(event);
    }

    public void removeWeaponSlot(int id) {
        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            if (weaponSlot.getId() == id) {
                ship.removeFixture(weaponSlot.getFixture());
                weaponSlots.remove(i);
                weaponSlot.onRemoved();
                return;
            }
        }
    }

    public void addFixturesToBody() {
        reactor.addFixtureToBody(ship);
        shield.addFixtureToBody(ship);
        engines.addFixtureToBody(ship);
    }

    public void destroyModule(int id, ModuleType type) {
        List<Module> modules = modulesByType.get(type);
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            if (module.getId() == id) {
                module.setDead();
                modules.remove(i);
                break;
            }
        }

        for (int i = 0; i < moduleList.size(); i++) {
            Module module = moduleList.get(i);
            if (module.getType() == type && module.getId() == id) {
                moduleList.remove(i);
                break;
            }
        }
    }

    public void removeShield() {
        destroyModule(shield.getId(), shield.getType());
        shield = null;
    }

    public void disableShield() {
        if (shield != null) {
            shield.removeShield();
        }
    }

    public List<Module> getModulesByType(ModuleType moduleType) {
        return modulesByType.get(moduleType);
    }
}