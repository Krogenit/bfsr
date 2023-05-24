package net.bfsr.network.packet.input;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

import java.io.IOException;

@Getter
@NoArgsConstructor
public class PacketSyncPlayerMousePosition extends PacketAdapter {
    private final Vector2f mousePosition = new Vector2f();

    public PacketSyncPlayerMousePosition(Vector2f mousePosition) {
        this.mousePosition.set(mousePosition);
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        ByteBufUtils.writeVector(data, mousePosition);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        ByteBufUtils.readVector(data, mousePosition);
    }
}