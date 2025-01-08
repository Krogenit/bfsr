package net.bfsr.engine.renderer.buffer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BufferLock {
    private int bufferingIndex;
    private long fenceSync;
}
