package net.bfsr.engine.util;

public enum Side {
    CLIENT, SERVER;

    public boolean isServer() {
        return this == SERVER;
    }

    public boolean isClient() {
        return this == CLIENT;
    }
}