package net.bfsr.client.network.manager;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.network.pipeline.MessageDecoderUDP;
import net.bfsr.client.network.pipeline.MessageHandlerUDP;
import net.bfsr.client.network.pipeline.PacketEncoder;
import net.bfsr.network.PacketOut;

import java.net.InetAddress;

@Log4j2
public class NetworkManagerUDP {
    private Channel channel;
    private EventLoopGroup workGroup;

    public ChannelFuture connect(NetworkSystem networkSystem, InetAddress address, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workGroup = new NioEventLoopGroup());
        bootstrap.channel(NioDatagramChannel.class);
        bootstrap.option(ChannelOption.SO_BROADCAST, true);
        bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(DatagramChannel datagramChannel) {
                datagramChannel.pipeline().addLast("decoder", new MessageDecoderUDP());
                datagramChannel.pipeline().addLast("encoder", new PacketEncoder(networkSystem));
                datagramChannel.pipeline().addLast("handler", new MessageHandlerUDP(networkSystem));
            }
        });

        ChannelFuture channelFuture = bootstrap.connect(address, port).syncUninterruptibly();
        channel = channelFuture.channel();
        log.info("Connected to server {}:{} with protocol UDP", address, port);
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
        channel.close();
    }

    public void shutdown() {
        workGroup.shutdownGracefully();
    }

    public boolean isChannelOpen() {
        return channel.isOpen();
    }
}