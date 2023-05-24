package net.bfsr.network.packet.common;

import net.bfsr.network.packet.PacketAdapter;

public class PacketKeepAlive extends PacketAdapter {
    @Override
    public boolean isAsync() {
        return true;
    }
}