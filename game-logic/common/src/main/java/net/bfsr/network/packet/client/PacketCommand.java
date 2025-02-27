package net.bfsr.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.command.Command;
import net.bfsr.engine.network.packet.Packet;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.util.ByteBufUtils;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketCommand extends PacketAdapter {
    private int command;
    private String[] args;

    public PacketCommand(Command command, Object... args) {
        this.command = command.ordinal();
        this.args = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            this.args[i] = args[i].toString();
        }
    }

    public static Packet spawnShip(int id, float x, float y) {
        return new PacketCommand(Command.SPAWN_SHIP, id, x, y);
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