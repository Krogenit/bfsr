package net.bfsr.network.packet.client.input;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.network.packet.PacketIdRegistry;

@Getter
@NoArgsConstructor
@PacketAnnotation(id = PacketIdRegistry.SHIP_TOGGLE_WEAPON)
public class PacketToggleWeapon extends PacketAdapter {}
