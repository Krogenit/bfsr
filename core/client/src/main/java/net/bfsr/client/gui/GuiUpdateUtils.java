package net.bfsr.client.gui;

import java.util.List;

public final class GuiUpdateUtils {
    public static <T extends GuiObject> T updateGuiObjectsHover(T hoveredObject, List<T> guiObjects) {
        int size = guiObjects.size();

        for (int i = 0; i < size; i++) {
            T guiObject = guiObjects.get(i);

            guiObject.updateMouseHover();
            if (guiObject.isMouseHover()) {
                if (hoveredObject != null && hoveredObject != guiObject) {
                    hoveredObject.setMouseHover(false);
                }
                hoveredObject = guiObject;
            }

            if (guiObject instanceof GuiObjectsHandler guiObjectsHandler) {
                hoveredObject = (T) updateGuiObjectsHover(hoveredObject, guiObjectsHandler.getGuiObjects());
            }
        }

        return hoveredObject;
    }
}