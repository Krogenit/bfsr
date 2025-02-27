package net.bfsr.engine.network.pipeline.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

public class FrameDecoder extends ByteToMessageDecoder {
    private final int maxFrameLength = Short.MAX_VALUE << 1;
    private final int lengthFieldOffset = 0;
    private final int lengthFieldLength = 2;
    private final int initialBytesToStrip = 2;

    private boolean discardingTooLongFrame;
    private int frameLengthInt = -1;
    private long tooLongFrameLength;
    private long bytesToDiscard;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        long frameLength = 0;
        if (frameLengthInt == -1) { // new frame
            if (discardingTooLongFrame) discardingTooLongFrame(byteBuf);

            if (byteBuf.readableBytes() < lengthFieldLength) return;

            int actualLengthFieldOffset = byteBuf.readerIndex() + lengthFieldOffset;
            frameLength = byteBuf.getUnsignedShort(actualLengthFieldOffset);

            if (frameLength < 0) {
                byteBuf.skipBytes(lengthFieldLength);
                throw new CorruptedFrameException("negative pre-adjustment length field: " + frameLength);
            }

            frameLength += lengthFieldLength;

            if (frameLength > maxFrameLength) {
                exceededFrameLength(byteBuf, frameLength);
                return;
            }
            // never overflows because it's less than maxFrameLength
            frameLengthInt = (int) frameLength;
        }
        if (byteBuf.readableBytes() < frameLengthInt) return;

        if (initialBytesToStrip > frameLengthInt) {
            byteBuf.skipBytes((int) frameLength);
            throw new CorruptedFrameException(
                    "Adjusted frame length (" + frameLength + ") is less " + "than initialBytesToStrip: " + initialBytesToStrip);
        }
        byteBuf.skipBytes(initialBytesToStrip);

        // extract frame
        int readerIndex = byteBuf.readerIndex();
        int actualFrameLength = frameLengthInt - initialBytesToStrip;
        ByteBuf frame = byteBuf.retainedSlice(readerIndex, actualFrameLength);
        byteBuf.readerIndex(readerIndex + actualFrameLength);
        frameLengthInt = -1; // start processing the next frame
        list.add(frame);
    }

    private void discardingTooLongFrame(ByteBuf in) {
        long bytesToDiscard = this.bytesToDiscard;
        int localBytesToDiscard = (int) Math.min(bytesToDiscard, in.readableBytes());
        in.skipBytes(localBytesToDiscard);
        bytesToDiscard -= localBytesToDiscard;
        this.bytesToDiscard = bytesToDiscard;

        failIfNecessary(false);
    }

    private void exceededFrameLength(ByteBuf in, long frameLength) {
        long discard = frameLength - in.readableBytes();
        tooLongFrameLength = frameLength;

        if (discard < 0) {
            // buffer contains more bytes then the frameLength so we can discard all now
            in.skipBytes((int) frameLength);
        } else {
            // Enter the discard mode and discard everything received so far.
            discardingTooLongFrame = true;
            bytesToDiscard = discard;
            in.skipBytes(in.readableBytes());
        }
        failIfNecessary(true);
    }

    private void failIfNecessary(boolean firstDetectionOfTooLongFrame) {
        if (bytesToDiscard == 0) {
            // Reset to the initial state and tell the handlers that
            // the frame was too large.
            long tooLongFrameLength = this.tooLongFrameLength;
            this.tooLongFrameLength = 0;
            discardingTooLongFrame = false;
            if (firstDetectionOfTooLongFrame) {
                fail(tooLongFrameLength);
            }
        } else {
            // Keep discarding and notify handlers if necessary.
            if (firstDetectionOfTooLongFrame) {
                fail(tooLongFrameLength);
            }
        }
    }

    private void fail(long frameLength) {
        if (frameLength > 0) {
            throw new TooLongFrameException("Adjusted frame length exceeds " + maxFrameLength + ": " + frameLength + " - discarded");
        } else {
            throw new TooLongFrameException("Adjusted frame length exceeds " + maxFrameLength + " - discarding");
        }
    }
}