package net.bfsr.server.network.packet;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.bfsr.network.Packet;
import net.bfsr.server.network.packet.client.*;
import net.bfsr.server.network.packet.common.*;
import net.bfsr.server.network.packet.server.component.*;
import net.bfsr.server.network.packet.server.effect.PacketSpawnParticleEffect;
import net.bfsr.server.network.packet.server.entity.PacketRemoveObject;
import net.bfsr.server.network.packet.server.entity.bullet.PacketSpawnBullet;
import net.bfsr.server.network.packet.server.entity.ship.PacketDestroyingShip;
import net.bfsr.server.network.packet.server.entity.ship.PacketShipInfo;
import net.bfsr.server.network.packet.server.entity.ship.PacketShipSetSpawned;
import net.bfsr.server.network.packet.server.entity.ship.PacketSpawnShip;
import net.bfsr.server.network.packet.server.entity.wreck.PacketShipWreck;
import net.bfsr.server.network.packet.server.entity.wreck.PacketSpawnWreck;
import net.bfsr.server.network.packet.server.entity.wreck.PacketSyncDamage;
import net.bfsr.server.network.packet.server.gui.PacketOpenGui;
import net.bfsr.server.network.packet.server.login.PacketDisconnectLogin;
import net.bfsr.server.network.packet.server.login.PacketJoinGame;
import net.bfsr.server.network.packet.server.login.PacketLoginTCPSuccess;
import net.bfsr.server.network.packet.server.login.PacketLoginUDPSuccess;
import net.bfsr.server.network.packet.server.player.PacketSetPlayerShip;

import java.lang.reflect.InvocationTargetException;

public class PacketRegistry {
    private final TIntObjectMap<Class<? extends Packet>> packetRegistry = new TIntObjectHashMap<>();
    private final TObjectIntMap<Class<? extends Packet>> packetRegistryInverse = new TObjectIntHashMap<>();
    private int id;

    public void registerPackets() {
        registerPacket(PacketHandshake.class);
        registerPacket(PacketLoginTCP.class);
        registerPacket(PacketLoginTCPSuccess.class);
        registerPacket(PacketLoginUDP.class);
        registerPacket(PacketLoginUDPSuccess.class);
        registerPacket(PacketDisconnectLogin.class);
        registerPacket(PacketJoinGame.class);
        registerPacket(PacketKeepAlive.class);
        registerPacket(PacketPing.class);
        registerPacket(PacketPauseGame.class);
        registerPacket(PacketChatMessage.class);
        registerPacket(PacketOpenGui.class);
        registerPacket(PacketCameraPosition.class);
        registerPacket(PacketCommand.class);
        registerPacket(PacketFactionSelect.class);
        registerPacket(PacketNeedObjectInfo.class);
        registerPacket(PacketRespawn.class);
        registerPacket(PacketShipControl.class);
        registerPacket(PacketObjectPosition.class);
        registerPacket(PacketShipEngine.class);
        registerPacket(PacketWeaponShoot.class);
        registerPacket(PacketArmorInfo.class);
        registerPacket(PacketDestroyingShip.class);
        registerPacket(PacketHullInfo.class);
        registerPacket(PacketRemoveObject.class);
        registerPacket(PacketSetPlayerShip.class);
        registerPacket(PacketShieldInfo.class);
        registerPacket(PacketShieldRebuild.class);
        registerPacket(PacketShieldRebuildingTime.class);
        registerPacket(PacketShieldRemove.class);
        registerPacket(PacketShipInfo.class);
        registerPacket(PacketSpawnBullet.class);
        registerPacket(PacketSpawnShip.class);
        registerPacket(PacketSpawnWreck.class);
        registerPacket(PacketShipWreck.class);
        registerPacket(PacketSyncDamage.class);
        registerPacket(PacketShipSetSpawned.class);
        registerPacket(PacketSpawnParticleEffect.class);
    }

    private void registerPacket(Class<? extends Packet> packetClass) {
        packetRegistry.put(id, packetClass);
        packetRegistryInverse.put(packetClass, id++);
    }

    public int getPacketId(Packet packet) {
        return packetRegistryInverse.get(packet.getClass());
    }

    public PacketIn createPacket(int packetId) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return (PacketIn) packetRegistry.get(packetId).getConstructor().newInstance();
    }
}