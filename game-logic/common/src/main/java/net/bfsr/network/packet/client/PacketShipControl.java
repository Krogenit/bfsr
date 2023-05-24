package net.bfsr.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PacketShipControl extends PacketAdapter {
    private int id;
    private boolean control;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeBoolean(control);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        control = data.readBoolean();
    }
}