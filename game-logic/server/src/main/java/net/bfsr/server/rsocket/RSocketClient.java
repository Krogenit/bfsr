package net.bfsr.server.rsocket;

import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Mono;

public class RSocketClient {
    private final RSocketStrategies rSocketStrategies = RSocketStrategies.builder()
            .decoder(new Jackson2CborDecoder())
            .encoder(new Jackson2CborEncoder())
            .build();

    private RSocketRequester rSocketRequester;

    public void connect(String host, int port) {
        rSocketRequester = RSocketRequester.builder()
                .rsocketStrategies(rSocketStrategies)
                .tcp(host, port);
    }

    public <RETURN_DATA> Mono<RETURN_DATA> request(String route, Object data, Class<RETURN_DATA> returnDataClass) {
        return rSocketRequester.route(route).data(data).retrieveMono(returnDataClass);
    }

    public Mono<Void> fireAndForget(String route, Object data) {
        return rSocketRequester.route(route).data(data).send();
    }

    public Mono<Void> fireAndForget(String route) {
        return rSocketRequester.route(route).send();
    }

    public void clear() {
        if (rSocketRequester != null) {
            rSocketRequester.dispose();
        }
    }
}