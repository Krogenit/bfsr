package net.bfsr.engine.renderer.buffer;

public class DisableSyncLockManager extends LockManager {
    @Override
    public void waitForLockedRange(int bufferingIndex) {}

    @Override
    public void lockRange(int bufferingIndex) {}
}
