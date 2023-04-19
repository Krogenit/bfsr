package net.bfsr.server.dto.converter;

import net.bfsr.server.dto.PlayerModel;
import net.bfsr.server.player.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = ShipConverter.class)
public interface PlayerConverter {
    PlayerConverter INSTANCE = Mappers.getMapper(PlayerConverter.class);

    @Mapping(source = "name", target = "username")
    @Mapping(target = "playerShip", ignore = true)
    @Mapping(target = "networkHandler", ignore = true)
    @Mapping(target = "digest", ignore = true)
    Player from(PlayerModel playerModel);

    @Mapping(source = "username", target = "name")
    PlayerModel to(Player playerServer);
}