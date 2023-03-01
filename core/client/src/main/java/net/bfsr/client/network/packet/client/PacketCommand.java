package net.bfsr.client.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.command.Command;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;

import java.io.IOException;

@NoArgsConstructor
public class PacketCommand implements PacketOut {
    private int command;
    private String[] args;

    public PacketCommand(Command command, String... args) {
        this.command = command.ordinal();
        this.args = args;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(command);
        data.writeInt(args.length);
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            ByteBufUtils.writeString(data, arg);
        }
    }
}