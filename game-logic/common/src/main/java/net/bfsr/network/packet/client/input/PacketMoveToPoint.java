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
@PacketAnnotation(id = PacketIdRegistry.SHIP_MOVE_TO_POINT)
public class PacketMoveToPoint extends PacketAdapter {
    private final Vector2f point = new Vector2f();

    public PacketMoveToPoint(float x, float y) {
        point.set(x, y);
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        ByteBufUtils.writeVector(data, point);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        ByteBufUtils.readVector(data, point);
    }
}
