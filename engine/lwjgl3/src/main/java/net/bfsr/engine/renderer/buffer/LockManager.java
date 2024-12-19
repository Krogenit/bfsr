package net.bfsr.engine.renderer.buffer;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL32.GL_ALREADY_SIGNALED;
import static org.lwjgl.opengl.GL32.GL_CONDITION_SATISFIED;
import static org.lwjgl.opengl.GL32.GL_SYNC_FLUSH_COMMANDS_BIT;
import static org.lwjgl.opengl.GL32.GL_SYNC_GPU_COMMANDS_COMPLETE;
import static org.lwjgl.opengl.GL32.GL_TIMEOUT_IGNORED;
import static org.lwjgl.opengl.GL32.GL_WAIT_FAILED;
import static org.lwjgl.opengl.GL32.glClientWaitSync;
import static org.lwjgl.opengl.GL32.glDeleteSync;
import static org.lwjgl.opengl.GL32.glFenceSync;

public class LockManager implements AbstractLockManager {
    private List<BufferLock> bufferLocks = new ArrayList<>();

    @Override
    public void waitForLockedRange(int bufferingIndex) {
        List<BufferLock> swapLocks = new ArrayList<>(1);
        for (int i = 0; i < bufferLocks.size(); i++) {
            BufferLock bufferLock = bufferLocks.get(i);
            if (bufferLock.getBufferingIndex() == bufferingIndex) {
                waitSync(bufferLock.getFenceSync());
                cleanup(bufferLock);
            } else {
                swapLocks.add(bufferLock);
            }
        }

        bufferLocks = swapLocks;
    }

    @Override
    public void lockRange(int bufferingIndex) {
        long fenceSync = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
        BufferLock newLock = new BufferLock(bufferingIndex, fenceSync);
        bufferLocks.add(newLock);
    }

    private void waitSync(long fenceSync) {
        long waitDuration = 0;
        int waitFlags = 0;
        while (true) {
            int waitRet = glClientWaitSync(fenceSync, waitFlags, waitDuration);
            if (waitRet == GL_ALREADY_SIGNALED || waitRet == GL_CONDITION_SATISFIED) {
                return;
            } else if (waitRet == GL_WAIT_FAILED) {
                throw new RuntimeException("GL Client Wait Sync error");
            }

            waitFlags = GL_SYNC_FLUSH_COMMANDS_BIT;
            waitDuration = GL_TIMEOUT_IGNORED;
        }
    }

    private void cleanup(BufferLock bufferLock) {
        glDeleteSync(bufferLock.getFenceSync());
    }

    @Override
    public void clear() {
        for (int i = 0; i < bufferLocks.size(); i++) {
            cleanup(bufferLocks.get(i));
        }

        bufferLocks.clear();
    }
}