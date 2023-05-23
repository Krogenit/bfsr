package net.bfsr.client.network.manager;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.network.pipeline.MessageDecoderTCP;
import net.bfsr.client.network.pipeline.MessageHandlerTCP;
import net.bfsr.client.network.pipeline.PacketEncoder;
import net.bfsr.network.PacketOut;
import net.bfsr.network.pipeline.tcp.FrameDecoder;
import net.bfsr.network.pipeline.tcp.LengthPrepender;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

@Log4j2
public class NetworkManagerTCP {
    private Channel channel;
    private EventLoopGroup workGroup;

    public ChannelFuture connect(NetworkSystem networkSystem, InetAddress address, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workGroup = new NioEventLoopGroup());
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(@NotNull SocketChannel socketChannel) {
                socketChannel.pipeline().addLast("slicer", new FrameDecoder());
                socketChannel.pipeline().addLast("prepender", new LengthPrepender());

                socketChannel.pipeline().addLast("decoder", new MessageDecoderTCP());
                socketChannel.pipeline().addLast("encoder", new PacketEncoder(networkSystem));

                socketChannel.pipeline().addLast("handler", new MessageHandlerTCP(networkSystem));
            }
        });

        ChannelFuture channelFuture = bootstrap.connect(address, port).syncUninterruptibly();
        this.channel = channelFuture.channel();
        log.info("Connected to server {}:{} with protocol TCP", address, port);
        return channelFuture;
    }

    public void sendPacket(PacketOut packet) {
        if (channel.eventLoop().inEventLoop()) {
            channel.writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            channel.eventLoop().execute(() -> channel.writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE));
        }
    }

    public void closeChannel() {
        if (channel != null) {
            channel.close();
        }
    }

    public void shutdown() {
        if (workGroup != null) {
            workGroup.shutdownGracefully();
        }
    }

    public boolean isChannelOpen() {
        return channel != null && channel.isOpen();
    }
}