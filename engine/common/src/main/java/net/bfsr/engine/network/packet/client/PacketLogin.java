package net.bfsr.engine.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.CommonPacketRegistry;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.util.ByteBufUtils;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@PacketAnnotation(id = CommonPacketRegistry.LOGIN)
public class PacketLogin extends PacketAdapter {
    private String login;

    @Override
    public void write(ByteBuf data) throws IOException {
        ByteBufUtils.writeString(data, login);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        login = ByteBufUtils.readString(data);
    }
}