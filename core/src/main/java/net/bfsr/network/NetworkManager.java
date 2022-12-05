package net.bfsr.network;

import com.google.common.collect.BiMap;
import com.google.common.collect.Queues;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GenericFutureListener;
import net.bfsr.network.status.EnumConnectionState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Queue;

public abstract class NetworkManager extends SimpleChannelInboundHandler<Packet> {
    protected static final Logger logger = LogManager.getLogger();
    public static final AttributeKey<EnumConnectionState> attrKeyConnectionState = AttributeKey.valueOf("protocol");
    public static final AttributeKey<BiMap> attrKeyReceivable = AttributeKey.valueOf("receivable_packets");
    public static final AttributeKey<BiMap> attrKeySendable = AttributeKey.valueOf("sendable_packets");

    public final NioEventLoopGroup eventLoops = new NioEventLoopGroup();
    public static final NetworkStatistics statistics = new NetworkStatistics();

    private final boolean isClientSide;

    protected final Queue<Packet> receivedPacketsQueue = Queues.newConcurrentLinkedQueue();
    private final Queue<InboundHandlerTuplePacketListener> outboundPacketsQueue = Queues.newConcurrentLinkedQueue();
    protected Channel channel;
    private SocketAddress socketAddress;
    protected EnumConnectionState connectionState = EnumConnectionState.HANDSHAKING;
    private String terminationReason;

    public NetworkManager(boolean isClient) {
        this.isClientSide = isClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelActive(channelHandlerContext);
        this.channel = channelHandlerContext.channel();
        this.socketAddress = this.channel.remoteAddress();
        this.setConnectionState(EnumConnectionState.HANDSHAKING);
    }

    public void setConnectionState(EnumConnectionState connectionState) {
        this.connectionState = this.channel.attr(attrKeyConnectionState).getAndSet(connectionState);
        this.channel.attr(attrKeyReceivable).set(connectionState.getPacketsServerSide(this.isClientSide));
        this.channel.attr(attrKeySendable).set(connectionState.getPacketsClientSide(this.isClientSide));
        this.channel.config().setAutoRead(true);
        logger.debug("Enabled auto read");
    }

    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) {
        this.closeChannel("disconnect.endOfStream");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        String reason;

        if (throwable instanceof TimeoutException) {
            reason = "disconnect.timeout";
        } else {
            reason = "disconnect.genericReason";
            throwable.printStackTrace();
        }

        this.closeChannel(reason);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet packet) throws IOException {
        if (this.channel.isOpen()) {
            if (packet.hasPriority()) {
                processPacket(packet);
            } else {
                this.receivedPacketsQueue.add(packet);
            }
        }
    }

    public void scheduleOutboundPacket(Packet packet, GenericFutureListener<?>... listeners) {
        if (this.channel != null && this.channel.isOpen()) {
            this.flushOutboundQueue();
            this.dispatchPacket(packet, listeners);
        } else {
            this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packet, listeners));
        }
    }

    private void dispatchPacket(final Packet packet, final GenericFutureListener[] listeners) {
        final EnumConnectionState enumconnectionstate = EnumConnectionState.getConnectionState(packet);
        final EnumConnectionState enumconnectionstate1 = this.channel.attr(attrKeyConnectionState).get();

        if (enumconnectionstate1 != enumconnectionstate) {
            logger.debug("Disabled auto read");
            this.channel.config().setAutoRead(false);
        }

        if (this.channel.eventLoop().inEventLoop()) {
            if (enumconnectionstate != enumconnectionstate1) {
                this.setConnectionState(enumconnectionstate);
            }

            this.channel.writeAndFlush(packet).addListeners(listeners).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            this.channel.eventLoop().execute(() -> {
                if (enumconnectionstate != enumconnectionstate1) {
                    NetworkManager.this.setConnectionState(enumconnectionstate);
                }

                NetworkManager.this.channel.writeAndFlush(packet).addListeners(listeners).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            });
        }
    }

    private void flushOutboundQueue() {
        if (this.channel != null && this.channel.isOpen()) {
            while (!this.outboundPacketsQueue.isEmpty()) {
                NetworkManager.InboundHandlerTuplePacketListener inboundhandlertuplepacketlistener = this.outboundPacketsQueue.poll();
                this.dispatchPacket(inboundhandlertuplepacketlistener.packet, inboundhandlertuplepacketlistener.futureListeners);
            }
        }
    }

    public void processReceivedPackets() {
        this.flushOutboundQueue();
        EnumConnectionState enumconnectionstate = this.channel.attr(attrKeyConnectionState).get();

        if (this.connectionState != enumconnectionstate) {
            if (this.connectionState != null) {
                onConnectionStateTransition(enumconnectionstate);
            }

            this.connectionState = enumconnectionstate;
        }

        for (int i = 3000; !this.receivedPacketsQueue.isEmpty() && i >= 0; --i) {
            Packet packet = this.receivedPacketsQueue.poll();
            processPacket(packet);
        }

        if (connectionState != null) {
            update();
        }

        this.channel.flush();
    }

    protected abstract void update();

    protected abstract void processPacket(Packet packet);

    protected abstract void onConnectionStateTransition(EnumConnectionState newState);

    protected abstract void onDisconnect(String reason);

    public SocketAddress getSocketAddress() {
        return this.socketAddress;
    }

    public void closeChannel(String reason) {
        if (this.channel.isOpen()) {
            this.channel.close();
            this.terminationReason = reason;
        }
    }

    public boolean isLocalChannel() {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }

    public boolean isChannelOpen() {
        return this.channel != null && this.channel.isOpen();
    }

    public String getExitMessage() {
        return this.terminationReason;
    }

    public void disableAutoRead() {
        this.channel.config().setAutoRead(false);
    }

    public void stop() {
        eventLoops.shutdownGracefully();
    }

    public NioEventLoopGroup getEventLoops() {
        return eventLoops;
    }

    static class InboundHandlerTuplePacketListener {
        private final Packet packet;
        private final GenericFutureListener[] futureListeners;

        public InboundHandlerTuplePacketListener(Packet packet, GenericFutureListener... futureListeners) {
            this.packet = packet;
            this.futureListeners = futureListeners;
        }
    }
}