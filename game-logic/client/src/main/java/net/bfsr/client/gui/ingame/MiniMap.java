package net.bfsr.client.gui.ingame;

import net.bfsr.client.Core;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.object.TexturedGuiObject;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import net.bfsr.world.World;
import org.dyn4j.geometry.AABB;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.List;

public class MiniMap {
    private final AbstractRenderer renderer = Engine.renderer;
    private final AbstractGUIRenderer guiRenderer = renderer.guiRenderer;
    private final RenderManager renderManager = Core.get().getRenderManager();
    private final TexturedGuiObject map = new TexturedGuiObject(TextureRegister.guiHudShip);

    private final AABB boundingBox = new AABB(0);
    private final AABB shipAABB = new AABB(0);
    private final Vector4f color = new Vector4f();

    public void init(Gui gui) {
        int scaleX = 280;
        int scaleY = 220;

        map.setSize(scaleX, scaleY).atTopLeftCorner(0, 0);
        gui.registerGuiObject(map);
    }

    public void render(World world) {
        List<Ship> ships = world.getEntitiesByType(Ship.class);
        Vector2f camPos = renderer.camera.getPosition();
        float mapOffsetX = 600;
        float mapOffsetY = 600;
        boundingBox.set(camPos.x - mapOffsetX, camPos.y - mapOffsetY, camPos.x + mapOffsetX, camPos.y + mapOffsetY);
        float mapScaleX = 5.0f;
        float mapScaleY = 7.0f;
        float shipSize = 1.0f;
        renderer.glEnable(GL.GL_SCISSOR_TEST);
        int offsetY = 17;
        int offsetX = 22;
        renderer.glScissor(map.getX() + offsetX, renderer.getScreenHeight() - map.getHeight() + offsetY,
                map.getWidth() - (offsetX << 1), map.getHeight() - (offsetY << 1));

        int miniMapX = map.getX() + map.getWidth() / 2;
        int miniMapY = map.getY() + map.getHeight() / 2;
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
                    color.x = 0.5f;
                    color.y = 1.0f;
                    color.z = 0.5f;
                } else if (faction == Faction.HUMAN) {
                    color.x = 0.5f;
                    color.y = 0.5f;
                    color.z = 1.0f;
                } else {
                    color.x = 1.0f;
                    color.y = 1.0f;
                    color.z = 0.5f;
                }

                Render<?> render = renderManager.getRender(s.getId());
                if (render != null) {
                    Vector2f lastPosition = render.getLastPosition();
                    int lastX = (int) (miniMapX + (lastPosition.x - camPos.x) / mapScaleX);
                    int lastY = (int) (miniMapY + (lastPosition.y - camPos.y) / mapScaleY);
                    int x = (int) (miniMapX + (pos.x - camPos.x) / mapScaleX);
                    int y = (int) (miniMapY + (pos.y - camPos.y) / mapScaleY);
                    int sizeX = (int) (scale.x * shipSize);
                    int sizeY = (int) (scale.y * shipSize);
                    guiRenderer.add(lastX, lastY, x, y, render.getLastSin(), render.getLastCos(), s.getSin(), s.getCos(), sizeX,
                            sizeY, color.x, color.y, color.z, 1.0f, render.getTexture());
                }
            }
        }

        guiRenderer.render();
        renderer.glDisable(GL.GL_SCISSOR_TEST);
    }

    public int getHeight() {
        return map.getHeight();
    }
}