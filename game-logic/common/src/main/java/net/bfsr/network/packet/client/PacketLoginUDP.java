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
public class PacketLoginUDP extends PacketAdapter {
    private String login;
    private byte[] digest;

    @Override
    public void write(ByteBuf data) throws IOException {
        ByteBufUtils.writeString(data, login);
        data.writeByte(digest.length);
        data.writeBytes(digest);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        login = ByteBufUtils.readString(data);
        data.readBytes(digest = new byte[data.readByte()]);
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}