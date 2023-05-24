package net.bfsr.network;

import net.bfsr.network.packet.Packet;

public abstract class NetworkHandler {
    public abstract void addPacketToQueue(Packet packet);
}