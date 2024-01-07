package net.bfsr.server.network.manager;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.network.pipeline.MessageDecoderUDP;
import net.bfsr.server.network.pipeline.MessageHandlerUDP;
import net.bfsr.server.network.pipeline.PacketEncoderUDP;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

@Log4j2
public class NetworkManagerUDP {
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    @Getter
    private DatagramChannel channel;

    public void startup(NetworkSystem networkSystem, InetAddress address, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(bossGroup);
        bootstrap.channel(NioDatagramChannel.class);
        bootstrap.option(ChannelOption.SO_BROADCAST, true);
        bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(@NotNull DatagramChannel datagramChannel) {
                datagramChannel.pipeline().addLast("decoder", new MessageDecoderUDP());
                datagramChannel.pipeline().addLast("encoder", new PacketEncoderUDP(networkSystem));
                datagramChannel.pipeline().addLast("handler", new MessageHandlerUDP());
            }
        });

        ChannelFuture channelFuture = bootstrap.bind(address, port).syncUninterruptibly();
        channel = (DatagramChannel) channelFuture.channel();
        log.info("Server UDP started on address {}:{}", address, port);
    }

    public void shutdown() {
        channel.close().syncUninterruptibly();
        bossGroup.shutdownGracefully();
    }
}