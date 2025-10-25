package net.bfsr.engine.network.packet.client;

import net.bfsr.engine.network.packet.CommonPacketRegistry;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;

@PacketAnnotation(id = CommonPacketRegistry.HANDSHAKE)
public class PacketHandshake extends PacketAdapter {}