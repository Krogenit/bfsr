package net.bfsr.server.dto;

import net.bfsr.faction.Faction;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "player")
public record PlayerModel(
        @Id
        ObjectId id,
        @Indexed(unique = true)
        String name,
        Faction faction,
        List<ShipModel> ships
) {}