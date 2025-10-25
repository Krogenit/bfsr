package net.bfsr.network.packet.client.input;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@PacketAnnotation(id = PacketIdRegistry.CLIENT_PLAYER_INPUT)
public class PacketPlayerInput extends PacketAdapter {
    private static final int MOUSE_STATES_SIZE = 2;
    private static final int BUTTON_STATES_SIZE = 5;

    private int renderDelayInFrames;
    private int frame;

    private float mouseWorldX, mouseWorldY;

    /**
     * 0 - left mouse
     * 1 - right mouse
     */
    private boolean[] mouseStates;

    /**
     * 0 - W
     * 1 - A
     * 2 - S
     * 3 - D
     * 4 - X
     */
    private boolean[] buttonsStates;

    private float cameraX, cameraY;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(renderDelayInFrames);
        data.writeInt(frame);

        data.writeFloat(mouseWorldX);
        data.writeFloat(mouseWorldY);

        for (int i = 0; i < MOUSE_STATES_SIZE; i++) {
            data.writeBoolean(mouseStates[i]);
        }

        for (int i = 0; i < BUTTON_STATES_SIZE; i++) {
            data.writeBoolean(buttonsStates[i]);
        }

        data.writeFloat(cameraX);
        data.writeFloat(cameraY);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        renderDelayInFrames = data.readInt();
        frame = data.readInt();

        mouseWorldX = data.readFloat();
        mouseWorldY = data.readFloat();

        mouseStates = new boolean[MOUSE_STATES_SIZE];
        for (int i = 0; i < mouseStates.length; i++) {
            mouseStates[i] = data.readBoolean();
        }

        buttonsStates = new boolean[BUTTON_STATES_SIZE];
        for (int i = 0; i < buttonsStates.length; i++) {
            buttonsStates[i] = data.readBoolean();
        }

        cameraX = data.readFloat();
        cameraY = data.readFloat();
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
