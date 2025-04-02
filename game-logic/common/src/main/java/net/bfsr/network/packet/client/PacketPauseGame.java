package net.bfsr.network.packet.client;

import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.network.packet.PacketIdRegistry;

@PacketAnnotation(id = PacketIdRegistry.PAUSE_GAME)
public class PacketPauseGame extends PacketAdapter {}