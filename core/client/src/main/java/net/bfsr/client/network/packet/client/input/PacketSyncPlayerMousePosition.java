package net.bfsr.client.network.packet.client.input;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketSyncPlayerMousePosition implements PacketOut {
    private Vector2f mouseWorldPosition;

    @Override
    public void write(ByteBuf data) throws IOException {
        ByteBufUtils.writeVector(data, mouseWorldPosition);
    }
}