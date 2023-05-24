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
public class PacketCameraPosition extends PacketAdapter {
    private float x, y;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeFloat(x);
        data.writeFloat(y);
    }

    @Override
    public void read(ByteBuf data) {
        x = data.readFloat();
        y = data.readFloat();
    }
}