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

    public static <T extends GuiObject> T updateGuiObjectsHover(List<T> guiObjects) {
        int size = guiObjects.size();

        T hoverObject = null;
        for (int i = 0; i < size; i++) {
            T guiObject = guiObjects.get(i);
            guiObject.updateMouseHover();
            if (guiObject.isMouseHover()) {
                if (hoverObject != null) {
                    hoverObject.setMouseHover(false);
                }
                hoverObject = guiObject;

                if (guiObject instanceof GuiObjectsHandler guiObjectsHandler) {
                    hoverObject = (T) updateGuiObjectsHover(guiObjectsHandler.getGuiObjects());
                }
            }
        }

        return hoverObject;
    }
}