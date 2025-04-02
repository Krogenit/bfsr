package net.bfsr.engine.network.packet.server.login;

import net.bfsr.engine.network.packet.CommonPacketRegistry;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;

@PacketAnnotation(id = CommonPacketRegistry.LOGIN_SUCCESS)
public class PacketLoginSuccess extends PacketAdapter {
    @Override
    public boolean isAsync() {
        return true;
    }
}