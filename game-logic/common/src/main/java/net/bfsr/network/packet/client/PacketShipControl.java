package net.bfsr.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@PacketAnnotation(id = PacketIdRegistry.SHIP_CONTROL)
public class PacketShipControl extends PacketAdapter {
    private int id;
    private boolean control;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeBoolean(control);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        id = data.readInt();
        control = data.readBoolean();
    }
}