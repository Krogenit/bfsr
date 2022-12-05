package net.bfsr.network;

import com.google.common.collect.BiMap;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public abstract class Packet {
    private static final Logger logger = LogManager.getLogger();

    /**
     * Returns a packet instance, given the params: BiMap<int, (Packet) Class> and (int) id
     */
    public static Packet generatePacket(BiMap biMap, int id) {
        try {
            Class<?> oclass = (Class<?>) biMap.get(id);
            return oclass == null ? null : (Packet) oclass.newInstance();
        } catch (Exception exception) {
            logger.error("Couldn't create packet " + id, exception);
            return null;
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public abstract void read(PacketBuffer data) throws IOException;

    /**
     * Writes the raw packet data to the data stream.
     */
    public abstract void write(PacketBuffer data) throws IOException;

    public abstract void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player);

    public abstract void processOnClientSide(NetworkManagerClient networkManager);

    /**
     * If true, the network manager will process the packet immediately when received, otherwise it will queue it for processing. Currently true for:
     * Disconnect, LoginSuccess, KeepAlive, ServerQuery/Info, Ping/Pong
     */
    public boolean hasPriority() {
        return false;
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }
}