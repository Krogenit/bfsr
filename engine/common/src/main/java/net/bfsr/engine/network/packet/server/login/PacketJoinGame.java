package net.bfsr.engine.network.packet.server.login;

import net.bfsr.engine.network.packet.CommonPacketRegistry;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;

@PacketAnnotation(id = CommonPacketRegistry.JOIN_GAME)
public class PacketJoinGame extends PacketAdapter {}