package net.bfsr.engine.renderer.buffer;

public class DisableSyncLockManager extends LockManager {
    @Override
    public void waitForLockedRange(int bufferingIndex) {
        // Skip waiting because we don't use sync
    }

    @Override
    public void lockRange(int bufferingIndex) {
        // Skip locking because we don't use sync
    }
}
