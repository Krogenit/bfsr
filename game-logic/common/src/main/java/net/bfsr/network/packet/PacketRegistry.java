package net.bfsr.network.packet;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.bfsr.engine.util.Side;
import net.bfsr.network.NetworkHandler;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Locale;
import java.util.Set;

import static org.reflections.scanners.Scanners.SubTypes;

public class PacketRegistry<NET_HANDLER extends NetworkHandler> {
    private final TIntObjectMap<Class<? extends Packet>> packetRegistry = new TIntObjectHashMap<>();
    private final TObjectIntMap<Class<? extends Packet>> packetRegistryInverse = new TObjectIntHashMap<>();
    private final TMap<Class<? extends Packet>, PacketHandler<Packet, NET_HANDLER>> packetHandlerRegistry = new THashMap<>();
    private int id;

    public void registerPackets(Side side) {
        Reflections reflections = new Reflections("net.bfsr.network.packet");
        Set<Class<?>> subTypes = reflections.get(SubTypes.of(PacketAdapter.class).asClass());
        subTypes.forEach(aClass -> registerPacket((Class<? extends Packet>) aClass));

        reflections = new Reflections("net.bfsr." + side.toString().toLowerCase(Locale.ROOT) + ".network.packet.handler");

        Set<Class<?>> singletons = reflections.get(SubTypes.of(PacketHandler.class).asClass());
        singletons.forEach(aClass -> {
            try {
                ParameterizedType genericSuperclass = ((ParameterizedType) aClass.getGenericSuperclass());
                Class<? extends Packet> packetType = (Class<? extends Packet>) genericSuperclass.getActualTypeArguments()[0];
                registerPacketHandler(packetType, (PacketHandler<Packet, NET_HANDLER>) aClass.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("Can't register packet handler " + aClass.getName(), e);
            }
        });
    }

    private void registerPacket(Class<? extends Packet> packetClass) {
        packetRegistry.put(id, packetClass);
        packetRegistryInverse.put(packetClass, id++);
    }

    private void registerPacketHandler(Class<? extends Packet> packetClass, PacketHandler<Packet, NET_HANDLER> packetHandler) {
        packetHandlerRegistry.put(packetClass, packetHandler);
    }

    public int getPacketId(Packet packet) {
        return packetRegistryInverse.get(packet.getClass());
    }

    public Packet createPacket(int packetId) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return packetRegistry.get(packetId).getConstructor().newInstance();
    }

    public PacketHandler<Packet, NET_HANDLER> getPacketHandler(Packet packet) {
        return packetHandlerRegistry.get(packet.getClass());
    }
}