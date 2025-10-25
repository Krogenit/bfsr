package net.bfsr.server.network.manager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.network.pipeline.tcp.FrameDecoder;
import net.bfsr.engine.network.pipeline.tcp.LengthPrepender;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.pipeline.MessageDecoderTCP;
import net.bfsr.server.network.pipeline.MessageHandlerTCP;
import net.bfsr.server.network.pipeline.PacketEncoderTCP;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.function.Supplier;

@Log4j2
public class NetworkManagerTCP {
    private int connectionIds;
    private final EventLoopGroup bossLoopGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerLoopGroup = new NioEventLoopGroup();
    private Channel channel;

    public void startup(ServerGameLogic serverGameLogic, NetworkSystem networkSystem, InetAddress address, int port,
                        Supplier<DatagramChannel> datagramChannelSupplier) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossLoopGroup, workerLoopGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(@NotNull SocketChannel socketChannel) {
                log.info("New connection with id {} from {}", connectionIds, socketChannel.remoteAddress());
                PlayerNetworkHandler playerNetworkHandler = serverGameLogic.createPlayerNetworkHandler(connectionIds++, socketChannel,
                        datagramChannelSupplier.get(), true);
                socketChannel.pipeline().addLast("slicer", new FrameDecoder());
                socketChannel.pipeline().addLast("prepender", new LengthPrepender());

                socketChannel.pipeline().addLast("decoder", new MessageDecoderTCP(networkSystem));
                socketChannel.pipeline().addLast("encoder", new PacketEncoderTCP(networkSystem));

                socketChannel.pipeline().addLast("handler", new MessageHandlerTCP(playerNetworkHandler, networkSystem));
                networkSystem.registerHandler(playerNetworkHandler);
            }
        });

        ChannelFuture channelFuture = bootstrap.bind(address, port).syncUninterruptibly();
        channel = channelFuture.channel();
        log.info("Server TCP started on address {}:{}", address, port);
    }

    public void shutdown() {
        channel.close().syncUninterruptibly();
        bossLoopGroup.shutdownGracefully();
        workerLoopGroup.shutdownGracefully();
    }
}