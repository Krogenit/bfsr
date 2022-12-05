package net.bfsr.network.server;

import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.network.Packet;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;

public abstract class ServerPacket extends Packet {

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {

    }
}
