package net.bfsr.client.launch;

import net.bfsr.editor.EditorGameLogic;
import net.bfsr.engine.LWJGL3Engine;

public final class EditorMain {
    public static void main(String[] args) {
        new LWJGL3Engine(EditorGameLogic.class).run();
    }
}