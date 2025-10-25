package net.bfsr.engine.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.CommonPacketRegistry;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;

import java.io.IOException;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@PacketAnnotation(id = CommonPacketRegistry.REGISTER_UDP)
public class PacketRegisterUDP extends PacketAdapter {
    private int connectionId;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(connectionId);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        connectionId = data.readInt();
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}