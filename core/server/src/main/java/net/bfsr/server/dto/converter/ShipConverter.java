package net.bfsr.server.dto.converter;

import net.bfsr.server.dto.ShipModel;
import net.bfsr.server.entity.ship.Ship;
import org.mapstruct.*;

import java.lang.reflect.InvocationTargetException;

@Mapper(uses = WeaponConverter.class)
public interface ShipConverter {
    @Mapping(target = "className", expression = "java(ship.getClass().getName())")
    @Mapping(target = "weapons", source = "weaponSlots")
    ShipModel to(Ship ship);
    @Mappings({@Mapping(target = "weaponSlots", source = "weapons"),
            @Mapping(target = "rotation", ignore = true), @Mapping(target = "dead", ignore = true),
            @Mapping(target = "id", ignore = true), @Mapping(target = "upFixture", ignore = true),
            @Mapping(target = "hull", ignore = true), @Mapping(target = "velocity", ignore = true),
            @Mapping(target = "armor", ignore = true), @Mapping(target = "shield", ignore = true),
            @Mapping(target = "engine", ignore = true), @Mapping(target = "faction", ignore = true),
            @Mapping(target = "reactor", ignore = true), @Mapping(target = "cargo", ignore = true),
            @Mapping(target = "name", ignore = true), @Mapping(target = "controlledByPlayer", ignore = true),
            @Mapping(target = "target", ignore = true), @Mapping(target = "owner", ignore = true),
            @Mapping(target = "contours", ignore = true), @Mapping(target = "fixturesToAdd", ignore = true)})
    Ship from(ShipModel shipModel);

    @ObjectFactory
    default <T extends Ship> T to(ShipModel shipModel, @TargetType Class<T> entityClass) {
        try {
            return (T) Class.forName(shipModel.className()).getConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}