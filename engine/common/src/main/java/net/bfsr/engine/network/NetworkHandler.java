package net.bfsr.engine.network;

import net.bfsr.engine.network.packet.Packet;

public abstract class NetworkHandler {
    public abstract void addPacketToQueue(Packet packet);
}