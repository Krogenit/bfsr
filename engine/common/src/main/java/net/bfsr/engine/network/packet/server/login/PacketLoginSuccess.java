package net.bfsr.engine.network.packet.server.login;

import net.bfsr.engine.network.packet.PacketAdapter;

public class PacketLoginSuccess extends PacketAdapter {
    @Override
    public boolean isAsync() {
        return true;
    }
}