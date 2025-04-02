package net.bfsr.network.packet.client.input;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.util.ByteBufUtils;
import net.bfsr.network.packet.PacketIdRegistry;
import org.joml.Vector2f;

import java.io.IOException;

@Getter
@NoArgsConstructor
@PacketAnnotation(id = PacketIdRegistry.MOUSE_POSITION)
public class PacketMouseSyncPosition extends PacketAdapter {
    private final Vector2f mousePosition = new Vector2f();

    public PacketMouseSyncPosition(Vector2f mousePosition) {
        this.mousePosition.set(mousePosition);
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        ByteBufUtils.writeVector(data, mousePosition);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        ByteBufUtils.readVector(data, mousePosition);
    }
}