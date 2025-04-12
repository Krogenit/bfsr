package net.bfsr.engine.network;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import lombok.Getter;
import net.bfsr.engine.network.packet.Packet;

public abstract class NetworkHandler {
    @Getter
    private double ping;
    @Getter
    private double averagePing;
    private final DoubleList pingResults = new DoubleArrayList();

    public abstract void addPacketToQueue(Packet packet);

    public void addPingResult(double ping) {
        this.ping = ping;
        this.pingResults.add(ping);

        if (pingResults.size() > 100) {
            pingResults.removeFirst();
        }

        double allPings = 0.0f;
        for (int i = 0; i < pingResults.size(); i++) {
            allPings += pingResults.getDouble(i);
        }

        this.averagePing = allPings / pingResults.size();
    }
}