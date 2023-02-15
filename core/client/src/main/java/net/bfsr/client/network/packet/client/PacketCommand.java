package net.bfsr.client.network.packet.client;

import lombok.NoArgsConstructor;
import net.bfsr.command.Command;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

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
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(command);
        data.writeInt(args.length);
        for (String arg : args) {
            data.writeStringToBuffer(arg);
        }
    }
}