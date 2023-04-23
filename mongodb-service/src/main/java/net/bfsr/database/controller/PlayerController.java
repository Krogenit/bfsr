package net.bfsr.database.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.database.service.PlayerService;
import net.bfsr.server.dto.PlayerModel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Controller
@Log4j2
public class PlayerController {
    private final PlayerService service;

    @MessageMapping("player")
    public Mono<PlayerModel> getPlayer(String name) {
        return service.getPlayer(name);
    }

    @MessageMapping("save-player")
    public Mono<PlayerModel> savePlayer(PlayerModel playerModel) {
        return service.save(playerModel);
    }

    @MessageMapping("delete-all")
    public Mono<Void> deleteAll() {
        return service.deleteAll();
    }
}