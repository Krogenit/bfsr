package net.bfsr.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.util.ByteBufUtils;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@PacketAnnotation(id = PacketIdRegistry.CHAT_MESSAGE)
public class PacketChatMessage extends PacketAdapter {
    private String message;

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        message = ByteBufUtils.readString(data);
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        ByteBufUtils.writeString(data, message);
    }
}