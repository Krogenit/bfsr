package net.bfsr.network.packet.server.login;

import net.bfsr.network.packet.PacketAdapter;

public class PacketLoginUDPSuccess extends PacketAdapter {
    @Override
    public boolean isAsync() {
        return true;
    }
}