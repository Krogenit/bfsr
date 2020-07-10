package ru.krogenit.bfsr.network;

import com.google.common.collect.BiMap;
import io.netty.buffer.ByteBuf;
import ru.krogenit.bfsr.entity.ship.PlayerServer;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

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