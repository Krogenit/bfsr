package net.bfsr.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;
import net.bfsr.network.util.ByteBufUtils;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PacketLogin extends PacketAdapter {
    private String login;

    @Override
    public void write(ByteBuf data) throws IOException {
        ByteBufUtils.writeString(data, login);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        login = ByteBufUtils.readString(data);
    }
}