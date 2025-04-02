package net.bfsr.engine.network.packet.server.login;

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
@PacketAnnotation(id = CommonPacketRegistry.LOGIN_DISCONNECT)
public class PacketDisconnectLogin extends PacketAdapter {
    private String message;

    @Override
    public void write(ByteBuf data) throws IOException {
        ByteBufUtils.writeString(data, message);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        message = ByteBufUtils.readString(data);
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}