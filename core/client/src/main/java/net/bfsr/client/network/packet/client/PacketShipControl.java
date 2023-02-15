package net.bfsr.client.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShipControl implements PacketOut {
    private int id;
    private boolean control;

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeBoolean(control);
    }
}