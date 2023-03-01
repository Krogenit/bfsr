package net.bfsr.server.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.server.MainServer;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PacketShipEngine implements PacketOut, PacketIn {
    private int id;
    private int dir;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        dir = data.readInt();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeInt(dir);
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        MainServer.getInstance().getNetworkSystem().sendTCPPacketToAllExcept(new PacketShipEngine(id, dir), playerNetworkHandler.getPlayer());
    }
}