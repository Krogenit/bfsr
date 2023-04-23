package net.bfsr.database.service;

import lombok.RequiredArgsConstructor;
import net.bfsr.database.repository.PlayerRepository;
import net.bfsr.server.dto.PlayerModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository repository;

    public Mono<PlayerModel> getPlayer(String name) {
        return repository.findByName(name);
    }

    public Mono<PlayerModel> save(PlayerModel player) {
        return repository.save(player);
    }

    public Mono<Void> deleteByName(String name) {
        return repository.deleteByName(name);
    }

    public Mono<Void> deleteAll() {
        return repository.deleteAll();
    }
}