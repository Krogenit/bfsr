package net.bfsr.server.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
import net.bfsr.server.MainServer;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PacketShipEngine implements PacketIn, PacketOut {
    private int id;
    private int dir;

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        dir = data.readInt();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeInt(dir);
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager) {
        MainServer.getInstance().getNetworkSystem().sendPacketToAllExcept(new PacketShipEngine(id, dir), networkManager.getPlayer());
    }
}