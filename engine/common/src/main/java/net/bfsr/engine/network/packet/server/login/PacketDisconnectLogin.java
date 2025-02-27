package net.bfsr.engine.network.packet.server.login;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.util.ByteBufUtils;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PacketDisconnectLogin extends PacketAdapter {
    private String message;

    @Override
    public void write(ByteBuf data) throws IOException {
        ByteBufUtils.writeString(data, message);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        message = ByteBufUtils.readString(data);
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}