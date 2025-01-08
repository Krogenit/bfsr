package net.bfsr.editor.gui.inspection;

import net.bfsr.client.Client;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.gui.component.BlankGuiObject;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.ScrollPane;
import net.bfsr.engine.gui.renderer.RectangleRenderer;
import org.joml.Vector2f;

import java.util.List;

import static net.bfsr.editor.gui.EditorTheme.SELECTION_BLUE_COLOR;

public class InspectionPanelRenderer<PROPERTIES_TYPE extends PropertiesHolder> extends RectangleRenderer {
    private final GuiManager guiManager = Client.get().getGuiManager();
    private final InspectionPanel<PROPERTIES_TYPE> inspectionPanel;
    private final int elementHeight;
    private final int exactObjectSelectionOffsetY;

    private int selectionId;

    InspectionPanelRenderer(InspectionPanel<PROPERTIES_TYPE> inspectionPanel) {
        super(inspectionPanel);
        this.inspectionPanel = inspectionPanel;
        this.elementHeight = inspectionPanel.getElementHeight();
        this.exactObjectSelectionOffsetY = inspectionPanel.getExactObjectSelectionOffsetY();
    }

    @Override
    protected void create() {
        super.create();
        idList.add(selectionId = guiRenderer.add(0, 0, 1, 1, 1, 1, 1, 1));
    }

    @Override
    public void update() {
        super.update();
        updateMovableObject();
    }

    private void updateMovableObject() {
        InspectionEntry<?> movableObject = inspectionPanel.getMovableObject();
        if (movableObject == null) return;

        if (inspectionPanel.isIntersectsWithMouse()) {
            updateInsertingPreview((int) Engine.mouse.getGuiPosition().y);
        }
    }

    private void updateInsertingPreview(int mouseY) {
        InspectionEntry<?> movableObject = inspectionPanel.getMovableObject();
        GuiObject guiObject = guiManager.getHoveredGuiObject();
        if (guiObject instanceof InspectionEntry<?> inspectionEntry) {
            int sceneX = inspectionEntry.getSceneX();
            int sceneY = inspectionEntry.getSceneY();
            int betweenObjectsLineHeight = 4;
            if (mouseY < sceneY + exactObjectSelectionOffsetY) {
                updateSelection(sceneX, sceneY - betweenObjectsLineHeight / 2, inspectionEntry.getWidth(), betweenObjectsLineHeight);
            } else if (mouseY >= sceneY + elementHeight - exactObjectSelectionOffsetY) {
                if (inspectionEntry.isMaximized() && inspectionEntry.getGuiObjects().size() > 0) {
                    GuiObject guiObject1 = inspectionEntry.getGuiObjects().get(0);
                    updateSelection(guiObject1.getSceneX(), sceneY + elementHeight - betweenObjectsLineHeight / 2, guiObject1.getWidth(),
                            betweenObjectsLineHeight);
                } else {
                    updateSelection(sceneX, sceneY + elementHeight - betweenObjectsLineHeight / 2, inspectionEntry.getWidth(),
                            betweenObjectsLineHeight);
                }
            } else if (inspectionEntry != movableObject &&
                    !inspectionPanel.isInHierarchy(movableObject, (InspectionEntry<PROPERTIES_TYPE>) inspectionEntry)) {
                updateSelection(sceneX, sceneY, inspectionEntry.getWidth());
            }
        }
    }

    private void updateSelection(int x, int y, int width) {
        updateSelection(x, y, width, elementHeight);
    }

    private void updateSelection(int x, int y, int width, int height) {
        guiRenderer.setLastPosition(selectionId, x, y);
        guiRenderer.setPosition(selectionId, x, y);
        guiRenderer.setLastSize(selectionId, width, height);
        guiRenderer.setSize(selectionId, width, height);
        guiRenderer.setLastColor(selectionId, SELECTION_BLUE_COLOR.x, SELECTION_BLUE_COLOR.y, SELECTION_BLUE_COLOR.z,
                SELECTION_BLUE_COLOR.w);
        guiRenderer.setColor(selectionId, SELECTION_BLUE_COLOR.x, SELECTION_BLUE_COLOR.y, SELECTION_BLUE_COLOR.z, SELECTION_BLUE_COLOR.w);
    }

    @Override
    public void render() {
        super.render();

        InspectionEntry<?> movableObject = inspectionPanel.getMovableObject();
        if (movableObject == null) return;

        guiRenderer.addDrawCommand(selectionId);

        if (inspectionPanel.isIntersectsWithMouse()) {
            renderInsertingPreview();
        }

        Vector2f position = Engine.mouse.getGuiPosition();
        int mouseX = (int) position.x;
        int mouseY = (int) position.y;
        renderObject(movableObject, mouseX, mouseY - movableObject.getHeight());
    }

    private void renderObject(GuiObject guiObject, int x, int y) {
        int lastX = guiObject.getX();
        int lastY = guiObject.getY();
        GuiObject parent = guiObject.getParent();
        guiObject.setParent(BlankGuiObject.INSTANCE);
        guiObject.setPosition(x, y);
        guiObject.updateLastValues();
        guiObject.getRenderer().render();
        guiRenderer.render();
        guiObject.setParent(parent);
        guiObject.setPosition(lastX, lastY);
        guiObject.updateLastValues();
    }

    private void renderInsertingPreview() {
        InspectionEntry<?> movableObject = inspectionPanel.getMovableObject();
        GuiObject guiObject = guiManager.getHoveredGuiObject();
        if (guiObject instanceof InspectionEntry<?> inspectionEntry) {
            if (inspectionEntry != movableObject &&
                    !inspectionPanel.isInHierarchy(movableObject, (InspectionEntry<PROPERTIES_TYPE>) inspectionEntry)) {
                inspectionEntry.getRenderer().render();
            }
        } else {
            ScrollPane scrollPane = inspectionPanel.getScrollPane();
            int height = 0;
            List<GuiObject> subObjects = scrollPane.getGuiObjects();
            for (int i = 0; i < subObjects.size(); i++) {
                height += subObjects.get(i).getHeight();
            }

            int x1 = scrollPane.getSceneX();
            int y1 = scrollPane.getSceneY() + scrollPane.getHeight() - height - movableObject.getHeight();
            renderObject(movableObject, x1, y1);
        }
    }
}
