package net.bfsr.database.repository;

import net.bfsr.server.dto.PlayerModel;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PlayerRepository extends ReactiveMongoRepository<PlayerModel, String> {
    Mono<PlayerModel> findByName(String name);
    Mono<Void> deleteByName(String name);
}