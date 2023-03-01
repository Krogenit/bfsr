package net.bfsr.server.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.GameObject;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShipControl implements PacketIn {
    private int id;
    private boolean control;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        control = data.readBoolean();
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        GameObject obj = playerNetworkHandler.getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            ship.setControlledByPlayer(control);
        }
    }
}