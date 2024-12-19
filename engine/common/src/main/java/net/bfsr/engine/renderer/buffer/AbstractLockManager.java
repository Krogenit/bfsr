package net.bfsr.engine.renderer.buffer;

public interface AbstractLockManager {
    void waitForLockedRange(int bufferingIndex);
    void lockRange(int bufferingIndex);
    void clear();
}
