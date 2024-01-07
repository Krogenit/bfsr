package net.bfsr.engine.util;

public enum Side {
    CLIENT, SERVER;

    private static final Side[] VALUES = values();

    public boolean isServer() {
        return this == SERVER;
    }

    public boolean isClient() {
        return this == CLIENT;
    }

    public Side getOpposite() {
        return this == CLIENT ? SERVER : CLIENT;
    }

    public static Side get(byte index) {
        return VALUES[index];
    }
}