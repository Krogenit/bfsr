package ru.krogenit.bfsr.network.server;

import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.network.NetworkManager;
import ru.krogenit.bfsr.network.Packet;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

public abstract class ServerPacket extends Packet {

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {

    }
}
