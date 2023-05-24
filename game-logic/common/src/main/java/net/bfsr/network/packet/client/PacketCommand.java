package net.bfsr.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.command.Command;
import net.bfsr.network.packet.PacketAdapter;
import net.bfsr.network.util.ByteBufUtils;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketCommand extends PacketAdapter {
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

    @Override
    public void read(ByteBuf data) throws IOException {
        command = data.readInt();
        int size = data.readInt();
        args = new String[size];
        for (int i = 0; i < size; i++) {
            args[i] = ByteBufUtils.readString(data);
        }
    }
}