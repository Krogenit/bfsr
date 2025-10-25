package net.bfsr.editor.gui;

import net.bfsr.client.Client;
import net.bfsr.editor.gui.property.PolygonProperty;
import net.bfsr.editor.gui.ship.GuiShipEditor;
import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.renderer.RectangleRenderer;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.renderer.font.glyph.Font;
import net.bfsr.engine.renderer.font.string.StringOffsetType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.List;

import static net.bfsr.editor.gui.EditorTheme.setupContextMenuButton;

public class GuiVertex extends GuiObject {
    private final AbstractMouse mouse = Engine.getMouse();
    private final GuiShipEditor guiShipEditor;
    private final Vector2fPropertiesHolder vertex;
    private final Label indexLabel;

    private boolean movingByMouse;
    private int mouseClickX;
    private int mouseClickY;
    private float vertexStartX;
    private float vertexStartY;

    public GuiVertex(GuiShipEditor guiShipEditor, Vector2fPropertiesHolder vertex, Font font, int fontSize, int elementHeight,
                     int contextMenuStringOffsetX) {
        super(14, 14);
        this.guiShipEditor = guiShipEditor;
        this.vertex = vertex;

        PolygonProperty polygonProperty = guiShipEditor.getPolygonProperty();
        List<Vector2fPropertiesHolder> vertices = polygonProperty.getVertices();
        indexLabel = new Label(font, "" + vertices.indexOf(vertex), fontSize);
        add(indexLabel.atCenter(0, 0));

        setColor(0.2f, 0.2f, 0.2f, 0.75f);
        setHoverColor(0.8f, 0.8f, 0.8f, 0.75f);
        setRenderer(new RectangleRenderer(this));

        setRightReleaseConsumer((mouseX, mouseY) -> {
            Client client = Client.get();

            String title = "Remove vertex";
            Button removeVertexButton = new Button(font.getWidth(title, fontSize) + contextMenuStringOffsetX, elementHeight, title, font,
                    fontSize, contextMenuStringOffsetX / 2, 0, StringOffsetType.DEFAULT, (mouseX1, mouseY1) -> {
                polygonProperty.removeObject(vertex);
                guiShipEditor.removeVertex(this);
            });

            int x = mouseX;
            int y = mouseY - elementHeight;
            removeVertexButton.atBottomLeft(x, y);

            title = "Add vertex before";
            Button addVertexBeforeButton = new Button(font.getWidth(title, fontSize) + contextMenuStringOffsetX, elementHeight, title, font,
                    fontSize, contextMenuStringOffsetX / 2, 0, StringOffsetType.DEFAULT, (mouseX1, mouseY1) -> {
                int index = vertices.indexOf(vertex);
                if (index != -1) {
                    int beforeIndex = index - 1;
                    if (beforeIndex < 0) {
                        beforeIndex = vertices.size() - 1;
                    }

                    Vector2fPropertiesHolder prevVertex = vertices.get(beforeIndex);
                    Vector2fPropertiesHolder newVertex = new Vector2fPropertiesHolder((vertex.getX() + prevVertex.getX()) / 2,
                            (vertex.getY() + prevVertex.getY()) / 2);
                    polygonProperty.addObjectAt(index, newVertex);
                    guiShipEditor.addVertex(index, new GuiVertex(guiShipEditor, newVertex, font, fontSize, elementHeight,
                            contextMenuStringOffsetX));
                }
            });

            y -= elementHeight;
            addVertexBeforeButton.atBottomLeft(x, y);

            title = "Add vertex after";
            Button addVertexAfterButton = new Button(font.getWidth(title, fontSize) + contextMenuStringOffsetX, elementHeight, title, font,
                    fontSize, contextMenuStringOffsetX / 2, 0, StringOffsetType.DEFAULT, (mouseX1, mouseY1) -> {
                int index = vertices.indexOf(vertex);
                if (index != -1) {
                    int insertIndex = index + 1;
                    if (insertIndex >= vertices.size()) {
                        insertIndex = 0;
                    }

                    Vector2fPropertiesHolder prevVertex = vertices.get(insertIndex);
                    Vector2fPropertiesHolder newVertex = new Vector2fPropertiesHolder((vertex.getX() + prevVertex.getX()) / 2,
                            (vertex.getY() + prevVertex.getY()) / 2);
                    polygonProperty.addObjectAt(insertIndex, newVertex);
                    guiShipEditor.addVertex(insertIndex, new GuiVertex(guiShipEditor, newVertex, font, fontSize, elementHeight,
                            contextMenuStringOffsetX));
                }
            });

            y -= elementHeight;
            addVertexAfterButton.atBottomLeft(x, y);

            client.getGuiManager().openContextMenu(setupContextMenuButton(removeVertexButton),
                    setupContextMenuButton(addVertexBeforeButton), setupContextMenuButton(addVertexAfterButton));
        });

        atBottomLeft(() -> {
            updatePosition();
            return x;
        }, () -> {
            updatePosition();
            return y;
        });
    }

    @Override
    public void update(int mouseX, int mouseY) {
        super.update(mouseX, mouseY);

        PolygonProperty polygonProperty = guiShipEditor.getPolygonProperty();
        int index = polygonProperty.getVertices().indexOf(vertex);
        indexLabel.setString("" + index);

        AbstractCamera camera = Client.get().getCamera();
        float zoom = camera.getZoom();

        if (movingByMouse) {
            Vector2i mouseGuiPosition = mouse.getGuiPosition();
            int mdx = mouseGuiPosition.x - mouseClickX;
            int mdy = mouseGuiPosition.y - mouseClickY;
            vertex.setX(vertexStartX + mdx / zoom);
            vertex.setY(vertexStartY + mdy / zoom);

            polygonProperty.setInputBoxesValue(index, vertex.getX(), vertex.getY());
        }

        updatePosition();
    }

    private void updatePosition() {
        AbstractCamera camera = Client.get().getCamera();
        float zoom = camera.getZoom();

        float guiX = (vertex.getX() - camera.getPosition().x) * zoom - camera.getOrigin().x;
        float guiY = (vertex.getY() - camera.getPosition().y) * zoom - camera.getOrigin().y;

        setPosition(guiX - width / 2.0f, guiY - height / 2.0f);
    }

    @Override
    public @Nullable GuiObject mouseLeftClick(int mouseX, int mouseY) {
        GuiObject guiObject = super.mouseLeftClick(mouseX, mouseY);

        if (isMouseHover()) {
            movingByMouse = true;
            mouseClickX = mouseX;
            mouseClickY = mouseY;
            vertexStartX = vertex.getX();
            vertexStartY = vertex.getY();
            Engine.getGuiManager().setActiveGuiObject(this);
        }

        return guiObject;
    }

    @Override
    public @Nullable GuiObject mouseLeftRelease(int mouseX, int mouseY) {
        boolean wasMovingByMouse = movingByMouse;

        movingByMouse = false;

        if (wasMovingByMouse) {
            Engine.getGuiManager().setActiveGuiObject(null);
        }

        return wasMovingByMouse ? this : super.mouseLeftRelease(mouseX, mouseY);
    }

    public GuiObject setPosition(float x, float y) {
        return setPosition(Math.round(x), Math.round(y));
    }

    @Override
    public void remove() {
        super.remove();

        if (movingByMouse) {
            Engine.getGuiManager().setActiveGuiObject(null);
        }
    }
}
