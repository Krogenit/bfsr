package net.bfsr.network.packet.client.input;

import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.network.packet.PacketIdRegistry;

@PacketAnnotation(id = PacketIdRegistry.MOUSE_LEFT_RELEASE)
public class PacketMouseLeftRelease extends PacketAdapter {}