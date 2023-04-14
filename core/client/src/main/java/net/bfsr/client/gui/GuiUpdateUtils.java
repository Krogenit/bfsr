package net.bfsr.client.gui;

import java.util.List;

public final class GuiUpdateUtils {
    public static <T extends GuiObject> void setGuiObjectsHover(List<T> guiObjects, boolean value) {
        for (int i = 0; i < guiObjects.size(); i++) {
            T guiObject = guiObjects.get(i);
            guiObject.updateMouseHover();
            guiObject.setMouseHover(value);

            if (guiObject instanceof GuiObjectsHandler guiObjectsHandler) {
                setGuiObjectsHover(guiObjectsHandler.getGuiObjects(), value);
            }
        }
    }

    public static <T extends GuiObject> T updateGuiObjectsHover(T hoveredObject, List<T> guiObjects) {
        int size = guiObjects.size();

        for (int i = 0; i < size; i++) {
            T guiObject = guiObjects.get(i);

            boolean hoveredUpdated = false;
            if (guiObject instanceof GuiObjectsHandler guiObjectsHandler) {
                GuiObject newHoveredObject = updateGuiObjectsHover(hoveredObject, guiObjectsHandler.getGuiObjects());
                if (newHoveredObject != guiObject) {
                    hoveredObject = guiObject;
                    hoveredUpdated = true;
                }
            }

            guiObject.updateMouseHover();
            if (!hoveredUpdated && guiObject.isMouseHover()) {
                if (hoveredObject != null && hoveredObject != guiObject) {
                    hoveredObject.setMouseHover(false);
                }
                hoveredObject = guiObject;
            }
        }

        return hoveredObject;
    }
}