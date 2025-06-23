package net.bfsr.engine.network.packet.server.login;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.CommonPacketRegistry;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;

import java.io.IOException;

@Getter
@NoArgsConstructor
@PacketAnnotation(id = CommonPacketRegistry.LOGIN_SUCCESS)
public class PacketLoginSuccess extends PacketAdapter {
    private int tick;

    public PacketLoginSuccess(int tick) {
        this.tick = tick;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(tick);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        tick = data.readInt();
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}