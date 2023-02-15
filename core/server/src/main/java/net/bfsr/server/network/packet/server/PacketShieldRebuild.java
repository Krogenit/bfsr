package net.bfsr.server.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShieldRebuild implements PacketOut {
    private int id;

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
    }
}