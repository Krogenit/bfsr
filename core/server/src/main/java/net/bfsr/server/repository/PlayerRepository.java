package net.bfsr.server.repository;

import net.bfsr.server.dto.PlayerModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends MongoRepository<PlayerModel, String> {
    PlayerModel findByName(String name);
}