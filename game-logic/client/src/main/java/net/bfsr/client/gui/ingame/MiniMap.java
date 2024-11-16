package net.bfsr.client.gui.ingame;

import net.bfsr.client.Client;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import org.jbox2d.collision.AABB;
import org.joml.Vector2f;

import java.util.List;

public class MiniMap extends TexturedRectangle {
    private final Client client = Client.get();
    private final RenderManager renderManager = client.getRenderManager();
    private final AABB boundingBox = new AABB();
    private final AABB shipAABB = new AABB();

    public MiniMap() {
        super(TextureRegister.guiHudShip, 280, 220);
    }

    @Override
    public void render(AbstractGUIRenderer guiRenderer, int lastX, int lastY, int x, int y) {
        super.render(guiRenderer, lastX, lastY, x, y);
        guiRenderer.render();
        List<Ship> ships = client.getWorld().getEntitiesByType(Ship.class);
        Vector2f camPos = Engine.renderer.camera.getPosition();
        float mapOffsetX = 600;
        float mapOffsetY = 600;
        boundingBox.set(camPos.x - mapOffsetX, camPos.y - mapOffsetY, camPos.x + mapOffsetX, camPos.y + mapOffsetY);
        float mapScaleX = 5.0f;
        float mapScaleY = 7.0f;
        float shipSize = 1.0f;
        Engine.renderer.glEnable(GL.GL_SCISSOR_TEST);
        int offsetY = 17;
        int offsetX = 22;
        Engine.renderer.glScissor(this.x + offsetX, Engine.renderer.getScreenHeight() - height + offsetY,
                width - (offsetX << 1), height - (offsetY << 1));

        int miniMapX = this.x + width / 2;
        int miniMapY = this.y + height / 2;
        float r, g, b;
        for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            float shipX = ship.getX();
            float shipY = ship.getY();
            float shipSizeX = ship.getSizeX();
            float shipSizeY = ship.getSizeY();
            float sX = shipSizeX * shipSize / 2.0f;
            float sY = shipSizeY * shipSize / 2.0f;
            shipAABB.set(shipX - sX, shipY - sY, shipX + sX, shipY + sY);
            if (boundingBox.overlaps(shipAABB)) {
                Faction faction = ship.getFaction();
                if (faction == Faction.ENGI) {
                    r = 0.5f;
                    g = 1.0f;
                    b = 0.5f;
                } else if (faction == Faction.HUMAN) {
                    r = 0.5f;
                    g = 0.5f;
                    b = 1.0f;
                } else {
                    r = 1.0f;
                    g = 1.0f;
                    b = 0.5f;
                }

                Render render = renderManager.getRender(ship.getId());
                if (render != null) {
                    Vector2f lastPosition = render.getLastPosition();
                    int lastX1 = (int) (miniMapX + (lastPosition.x - camPos.x) / mapScaleX);
                    int lastY1 = (int) (miniMapY + (-lastPosition.y + camPos.y) / mapScaleY);
                    int x1 = (int) (miniMapX + (shipX - camPos.x) / mapScaleX);
                    int y1 = (int) (miniMapY + (-shipY + camPos.y) / mapScaleY);
                    int sizeX = (int) (shipSizeX * shipSize);
                    int sizeY = (int) (shipSizeY * shipSize);
                    guiRenderer.addRotated(lastX + lastX1, lastY + lastY1, x + x1, y + y1, render.getLastSin(), render.getLastCos(),
                            ship.getSin(), ship.getCos(), sizeX, sizeY, r, g, b, 1.0f, render.getTexture());
                }
            }
        }

        guiRenderer.render();
        Engine.renderer.glDisable(GL.GL_SCISSOR_TEST);
    }
}