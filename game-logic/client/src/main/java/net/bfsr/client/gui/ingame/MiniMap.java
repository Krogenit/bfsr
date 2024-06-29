package net.bfsr.client.gui.ingame;

import net.bfsr.client.Core;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import org.dyn4j.geometry.AABB;
import org.joml.Vector2f;

import java.util.List;

public class MiniMap extends TexturedRectangle {
    private final Core core = Core.get();
    private final RenderManager renderManager = core.getRenderManager();
    private final AABB boundingBox = new AABB(0);
    private final AABB shipAABB = new AABB(0);

    public MiniMap() {
        super(TextureRegister.guiHudShip, 280, 220);
    }

    @Override
    public void render(AbstractGUIRenderer guiRenderer, int lastX, int lastY, int x, int y) {
        super.render(guiRenderer, lastX, lastY, x, y);
        guiRenderer.render();
        List<Ship> ships = core.getWorld().getEntitiesByType(Ship.class);
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
            Ship s = ships.get(i);
            Vector2f pos = s.getPosition();
            Vector2f scale = s.getSize();
            float sX = scale.x * shipSize / 2.0f;
            float sY = scale.y * shipSize / 2.0f;
            shipAABB.set(pos.x - sX, pos.y - sY, pos.x + sX, pos.y + sY);
            if (boundingBox.overlaps(shipAABB)) {
                Faction faction = s.getFaction();
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

                Render render = renderManager.getRender(s.getId());
                if (render != null) {
                    Vector2f lastPosition = render.getLastPosition();
                    int lastX1 = (int) (miniMapX + (lastPosition.x - camPos.x) / mapScaleX);
                    int lastY1 = (int) (miniMapY + (-lastPosition.y + camPos.y) / mapScaleY);
                    int x1 = (int) (miniMapX + (pos.x - camPos.x) / mapScaleX);
                    int y1 = (int) (miniMapY + (-pos.y + camPos.y) / mapScaleY);
                    int sizeX = (int) (scale.x * shipSize);
                    int sizeY = (int) (scale.y * shipSize);
                    guiRenderer.addRotated(lastX + lastX1, lastY + lastY1, x + x1, y + y1, render.getLastSin(), render.getLastCos(),
                            s.getSin(), s.getCos(), sizeX, sizeY, r, g, b, 1.0f, render.getTexture());
                }
            }
        }

        guiRenderer.render();
        Engine.renderer.glDisable(GL.GL_SCISSOR_TEST);
    }
}