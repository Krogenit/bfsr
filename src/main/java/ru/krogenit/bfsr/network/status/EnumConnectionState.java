package ru.krogenit.bfsr.network.status;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.logging.log4j.LogManager;
import ru.krogenit.bfsr.network.packet.PacketHandshake;
import ru.krogenit.bfsr.network.packet.PacketLoginStart;
import ru.krogenit.bfsr.network.packet.PacketServerQuery;
import ru.krogenit.bfsr.network.packet.PacketPing;
import ru.krogenit.bfsr.network.Packet;
import ru.krogenit.bfsr.network.packet.client.*;
import ru.krogenit.bfsr.network.packet.common.*;
import ru.krogenit.bfsr.network.packet.server.*;
import ru.krogenit.bfsr.network.packet.server.PacketServerInfo;
import ru.krogenit.bfsr.network.packet.server.PacketLoginSuccess;

import java.util.Map;

public enum EnumConnectionState {
	HANDSHAKING(-1) {{
		this.registerClientPacket(0, PacketHandshake.class);
	}},
	PLAY(0) {{
			this.registerServerPacket(0, PacketKeepAlive.class);
			this.registerServerPacket(1, PacketJoinGame.class);
			this.registerServerPacket(2, PacketDisconnectPlay.class);
			this.registerServerPacket(3, PacketSpawnShip.class);
			this.registerServerPacket(4, PacketShipEngine.class);
			this.registerServerPacket(5, PacketObjectPosition.class);
			this.registerServerPacket(6, PacketSpawnBullet.class);
			this.registerServerPacket(7, PacketOpenGui.class);
			this.registerServerPacket(8, PacketShieldRebuild.class);
			this.registerServerPacket(9, PacketShieldRemove.class);
			this.registerServerPacket(10, PacketSetPlayerShip.class);
			this.registerServerPacket(11, PacketObjectSetDead.class);
			this.registerServerPacket(12, PacketShipFaction.class);
			this.registerServerPacket(13, PacketShipInfo.class);
			this.registerServerPacket(14, PacketShipSetWeaponSlot.class);
			this.registerServerPacket(15, PacketShipName.class);
			this.registerServerPacket(16, PacketRemoveObject.class);
			this.registerServerPacket(17, PacketShieldRebuildingTime.class);
			this.registerServerPacket(18, PacketChatMessage.class);
			this.registerServerPacket(19, PacketWeaponShoot.class);
			this.registerServerPacket(20, PacketDestroingShip.class);
			this.registerServerPacket(21, PacketSpawnParticle.class);
			this.registerServerPacket(22, PacketShieldInfo.class);
			this.registerServerPacket(23, PacketArmorInfo.class);
			this.registerServerPacket(24, PacketHullInfo.class);
			this.registerServerPacket(25, PacketPing.class);

			this.registerClientPacket(0, PacketKeepAlive.class);
			this.registerClientPacket(1, PacketShipEngine.class);
			this.registerClientPacket(2, PacketObjectPosition.class);
			this.registerClientPacket(3, PacketNeedObjectInfo.class);
			this.registerClientPacket(4, PacketKey.class);
			this.registerClientPacket(5, PacketCommand.class);
			this.registerClientPacket(6, PacketCameraPosition.class);
			this.registerClientPacket(7, PacketFactionSelect.class);
			this.registerClientPacket(8, PacketWeaponShoot.class);
			this.registerClientPacket(9, PacketChatMessage.class);
			this.registerClientPacket(10, PacketRespawn.class);
			this.registerClientPacket(11, PacketShipControl.class);
			this.registerClientPacket(12, PacketPing.class);
	}},
	STATUS(1) {{
			this.registerClientPacket(0, PacketServerQuery.class);
			this.registerServerPacket(0, PacketServerInfo.class);
	}},
	LOGIN(2) {{
			this.registerServerPacket(0, PacketDisconnectLogin.class);
			this.registerServerPacket(1, PacketLoginSuccess.class);
			this.registerClientPacket(0, PacketLoginStart.class);
	}};

	private static final TIntObjectMap<EnumConnectionState> states = new TIntObjectHashMap<>();
	public static final Map<Class<?>, EnumConnectionState> packetsMap = Maps.newHashMap();
	private final int stateInt;
	private final BiMap<Integer, Class<?>> clientPackets;
	private final BiMap<Integer, Class<?>> serverPackets;

	EnumConnectionState(int stateInt) {
		this.clientPackets = HashBiMap.create();
		this.serverPackets = HashBiMap.create();
		this.stateInt = stateInt;
	}

	protected void registerClientPacket(int id, Class<?> packetClass) {
		String s;

		if (this.clientPackets.containsKey(id)) {
			s = "Serverbound packet ID " + id + " is already assigned to " + this.clientPackets.get(id) + "; cannot re-assign to " + packetClass;
			LogManager.getLogger().fatal(s);
			throw new IllegalArgumentException(s);
		} else if (this.clientPackets.containsValue(packetClass)) {
			s = "Serverbound packet " + packetClass + " is already assigned to ID " + this.clientPackets.inverse().get(packetClass) + "; cannot re-assign to " + id;
			LogManager.getLogger().fatal(s);
			throw new IllegalArgumentException(s);
		} else {
			this.clientPackets.put(id, packetClass);
		}
	}

	protected void registerServerPacket(int id, Class<?> packetClass) {
		String s;

		if (this.serverPackets.containsKey(id)) {
			s = "Clientbound packet ID " + id + " is already assigned to " + this.serverPackets.get(id) + "; cannot re-assign to " + packetClass;
			LogManager.getLogger().fatal(s);
			throw new IllegalArgumentException(s);
		} else if (this.serverPackets.containsValue(packetClass)) {
			s = "Clientbound packet " + packetClass + " is already assigned to ID " + this.serverPackets.inverse().get(packetClass) + "; cannot re-assign to " + id;
			LogManager.getLogger().fatal(s);
			throw new IllegalArgumentException(s);
		} else {
			this.serverPackets.put(id, packetClass);
		}
	}

	public BiMap<Integer, Class<?>> getClientPackets() {
		return this.clientPackets;
	}

	public BiMap<Integer, Class<?>> getServerPackets() {
		return this.serverPackets;
	}

	public BiMap<Integer, Class<?>> getPacketsServerSide(boolean isServer) {
		return isServer ? this.getServerPackets() : this.getClientPackets();
	}

	public BiMap<Integer, Class<?>> getPacketsClientSide(boolean isClient) {
		return isClient ? this.getClientPackets() : this.getServerPackets();
	}

	public int getInt() {
		return this.stateInt;
	}

	public static EnumConnectionState getConnectionStateByInt(int state) {
		return states.get(state);
	}

	public static EnumConnectionState getConnectionState(Packet packet) {
		return packetsMap.get(packet.getClass());
	}

	static {
		for (EnumConnectionState state : values()) {
			states.put(state.getInt(), state);

			for (Class<?> aClass : Iterables.concat(state.getServerPackets().values(), state.getClientPackets().values())) {
				if (packetsMap.containsKey(aClass) && packetsMap.get(aClass) != state) {
					throw new Error("Packet " + aClass + " is already assigned to protocol " + packetsMap.get(aClass) + " - can't reassign to " + state);
				}

				packetsMap.put(aClass, state);
			}
		}
	}
}