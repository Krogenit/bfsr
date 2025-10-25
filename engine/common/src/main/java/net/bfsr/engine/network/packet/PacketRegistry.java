package net.bfsr.engine.network.packet;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.bfsr.engine.network.NetworkHandler;
import net.bfsr.engine.util.Side;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Locale;
import java.util.Set;

import static org.reflections.scanners.Scanners.SubTypes;

public class PacketRegistry<NET_HANDLER extends NetworkHandler> {
    private final TIntObjectMap<Class<? extends Packet>> packetRegistry = new TIntObjectHashMap<>();
    private final TObjectIntMap<Class<? extends Packet>> packetRegistryInverse = new TObjectIntHashMap<>();
    private final TMap<Class<? extends Packet>, PacketHandler<Packet, NET_HANDLER>> packetHandlerRegistry = new THashMap<>();

    public void registerPackets(Side side) {
        Reflections reflections = new Reflections("net.bfsr");
        Set<Class<?>> subTypes = reflections.get(SubTypes.of(Scanners.TypesAnnotated.with(PacketAnnotation.class)).asClass());
        subTypes.forEach(aClass -> {
            if (Modifier.isAbstract(aClass.getModifiers())) {
                return;
            }

            registerPacket((Class<? extends Packet>) aClass);
        });

        reflections = new Reflections("net.bfsr." + side.toString().toLowerCase(Locale.ENGLISH) + ".network.packet.handler");
        Set<Class<?>> singletons = reflections.get(SubTypes.of(PacketHandler.class).asClass());
        singletons.forEach(aClass -> {
            if (Modifier.isAbstract(aClass.getModifiers())) {
                return;
            }

            try {
                ParameterizedType genericSuperclass = ((ParameterizedType) aClass.getGenericSuperclass());
                Class<? extends Packet> packetType = (Class<? extends Packet>) genericSuperclass.getActualTypeArguments()[0];
                registerPacketHandler(packetType, (PacketHandler<Packet, NET_HANDLER>) aClass
                        .getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("Can't register packet handler " + aClass.getName(), e);
            }
        });

        String oppositeSideName = side.getOpposite().toString().toLowerCase(Locale.ENGLISH);
        packetRegistry.forEachValue(aClass -> {
            if (aClass.getPackageName().contains(oppositeSideName)) {
                PacketHandler<Packet, NET_HANDLER> packetHandler = getPacketHandler(aClass);
                if (packetHandler == null) {
                    throw new RuntimeException("Can't find packet handler for packet class " + aClass);
                }
            }

            return true;
        });
    }

    private void registerPacket(Class<? extends Packet> packetClass) {
        PacketAnnotation annotation = packetClass.getAnnotation(PacketAnnotation.class);
        if (annotation == null) {
            throw new RuntimeException("Can't find annotation for packet class " + packetClass.getSimpleName());
        }

        Class<? extends Packet> registeredPacket = packetRegistry.get(annotation.id());
        if (registeredPacket != null) {
            throw new IllegalStateException("Packet " + packetClass.getName() + " with id " + annotation.id() + " already registered! " +
                    "Registered packet " + registeredPacket.getName());
        }

        packetRegistry.put(annotation.id(), packetClass);
        packetRegistryInverse.put(packetClass, annotation.id());
    }

    private void registerPacketHandler(Class<? extends Packet> packetClass, PacketHandler<Packet, NET_HANDLER> packetHandler) {
        packetHandlerRegistry.put(packetClass, packetHandler);
    }

    public int getPacketId(Packet packet) {
        return packetRegistryInverse.get(packet.getClass());
    }

    public Packet createPacket(int packetId)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return packetRegistry.get(packetId).getConstructor().newInstance();
    }

    public PacketHandler<Packet, NET_HANDLER> getPacketHandler(Packet packet) {
        return packetHandlerRegistry.get(packet.getClass());
    }

    private PacketHandler<Packet, NET_HANDLER> getPacketHandler(Class<? extends Packet> packetClass) {
        return packetHandlerRegistry.get(packetClass);
    }
}