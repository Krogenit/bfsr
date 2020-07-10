package ru.krogenit.bfsr.network.client;

import ru.krogenit.bfsr.network.NetworkManager;
import ru.krogenit.bfsr.network.Packet;

public abstract class ClientPacket extends Packet {

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {

    }
}
