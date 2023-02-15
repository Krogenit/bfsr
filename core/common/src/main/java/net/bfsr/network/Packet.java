package net.bfsr.network;

public interface Packet {
    /**
     * If true, the network manager will process the packet immediately when received, otherwise it will queue it for processing. Currently true for:
     * Disconnect, LoginSuccess, KeepAlive, ServerQuery/Info, Ping/Pong
     */
    default boolean hasPriority() {
        return false;
    }
}