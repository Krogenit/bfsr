package net.bfsr.client.event.chat;

import lombok.Getter;
import net.bfsr.engine.event.Event;

@Getter
public class ChatMessageEvent extends Event {
    private String message;

    public ChatMessageEvent setMessage(String message) {
        this.message = message;
        return this;
    }
}
