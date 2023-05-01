package net.bfsr.server.dto.converter;

import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.entity.ship.Ship;
import net.bfsr.server.dto.ShipModel;
import org.mapstruct.*;

import java.util.List;

@Mapper(uses = WeaponConverter.class)
public abstract class ShipConverter {
    @Mapping(target = "name", expression = "java(ship.getShipData().getName())")
    @Mapping(target = "weapons", source = "weaponSlots")
    public abstract ShipModel to(Ship ship);

    @Mappings({@Mapping(target = "weaponSlots", source = "weapons"), @Mapping(target = "dead", ignore = true),
            @Mapping(target = "id", ignore = true), @Mapping(target = "upFixture", ignore = true),
            @Mapping(target = "hull", ignore = true), @Mapping(target = "velocity", ignore = true),
            @Mapping(target = "armor", ignore = true), @Mapping(target = "shield", ignore = true),
            @Mapping(target = "engine", ignore = true), @Mapping(target = "faction", ignore = true),
            @Mapping(target = "reactor", ignore = true), @Mapping(target = "cargo", ignore = true),
            @Mapping(target = "name", ignore = true), @Mapping(target = "controlledByPlayer", ignore = true),
            @Mapping(target = "target", ignore = true), @Mapping(target = "owner", ignore = true),
            @Mapping(target = "fixtures", ignore = true), @Mapping(target = "moveDirections", ignore = true),
            @Mapping(target = "contours", ignore = true), @Mapping(target = "fixturesToAdd", ignore = true)})
    public abstract Ship from(ShipModel shipModel);

    @AfterMapping
    public void initWeapons(ShipModel shipModel, @MappingTarget Ship ship) {
        List<WeaponSlot> weaponSlots = ship.getWeaponSlots();
        for (int i = 0; i < weaponSlots.size(); i++) {
            weaponSlots.get(i).init(i, ship);
        }
    }

    @ObjectFactory
    public <T extends Ship> T to(ShipModel shipModel, @TargetType Class<T> entityClass) {
        return (T) new Ship(ShipRegistry.INSTANCE.get(shipModel.name()));
    }
}