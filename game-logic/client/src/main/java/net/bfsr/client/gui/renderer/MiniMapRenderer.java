package net.bfsr.client.gui.renderer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.client.Client;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.renderer.RectangleTexturedRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.RigidBodyAddToWorldEvent;
import net.bfsr.event.entity.RigidBodyRemovedFromWorldEvent;
import net.bfsr.faction.Faction;
import org.jbox2d.collision.AABB;
import org.joml.Vector2f;

import java.util.List;

public class MiniMapRenderer extends RectangleTexturedRenderer {
    private final Client client = Client.get();
    private final AABB boundingBox = new AABB();
    private final AABB shipAABB = new AABB();
    private final float mapOffsetX = 600;
    private final float mapOffsetY = 600;
    private final float mapScaleX = 5.0f;
    private final float mapScaleY = 7.0f;
    private final Int2ObjectMap<MapEntity> entityIdToDrawIdMap = new Int2ObjectOpenHashMap<>();

    @Getter
    @Setter
    @RequiredArgsConstructor
    private static class MapEntity {
        private final int renderId;
        private boolean visible;
    }

    public MiniMapRenderer(GuiObject guiObject) {
        super(guiObject, TextureRegister.guiHudShip);
        client.getEventBus().register(this);
    }

    @Override
    protected void setLastUpdateValues() {
        super.setLastUpdateValues();

        int width = guiObject.getWidth();
        int height = guiObject.getHeight();

        Vector2f camPos = Engine.renderer.camera.getPosition();
        int miniMapX = guiObject.getX() + width / 2;
        int miniMapY = guiObject.getY() + height / 2;

        entityIdToDrawIdMap.int2ObjectEntrySet().forEach(entry -> {
            Ship ship = client.getWorld().getEntityById(entry.getIntKey());
            float shipX = ship.getX();
            float shipY = ship.getY();
            shipAABB.set(shipX, shipY, shipX, shipY);
            if (boundingBox.overlaps(shipAABB)) {
                int x1 = (int) (miniMapX + (shipX - camPos.x) / mapScaleX);
                int y1 = (int) (miniMapY + (shipY - camPos.y) / mapScaleY);
                int renderId = entry.getValue().getRenderId();
                guiRenderer.setLastPosition(renderId, x1, y1);
            }
        });
    }

    @Override
    public void update() {
        super.update();

        int width = guiObject.getWidth();
        int height = guiObject.getHeight();

        Vector2f camPos = Engine.renderer.camera.getPosition();
        boundingBox.set(camPos.x - mapOffsetX, camPos.y - mapOffsetY, camPos.x + mapOffsetX, camPos.y + mapOffsetY);

        int miniMapX = guiObject.getX() + width / 2;
        int miniMapY = guiObject.getY() + height / 2;
        entityIdToDrawIdMap.int2ObjectEntrySet().forEach(entry -> {
            Ship ship = client.getWorld().getEntityById(entry.getIntKey());
            float shipX = ship.getX();
            float shipY = ship.getY();
            shipAABB.set(shipX, shipY, shipX, shipY);
            MapEntity mapEntity = entry.getValue();
            if (boundingBox.overlaps(shipAABB)) {
                int x1 = (int) (miniMapX + (shipX - camPos.x) / mapScaleX);
                int y1 = (int) (miniMapY + (shipY - camPos.y) / mapScaleY);
                int renderId = mapEntity.getRenderId();
                guiRenderer.setPosition(renderId, x1, y1);
                mapEntity.setVisible(true);
            } else {
                mapEntity.setVisible(false);
            }
        });
    }

    @EventHandler
    public EventListener<RigidBodyAddToWorldEvent> onShipAddedToWorld() {
        return event -> {
            if (event.getRigidBody() instanceof Ship ship) {
                int x = guiObject.getSceneX();
                int y = guiObject.getSceneY();
                int width = guiObject.getWidth();
                int height = guiObject.getHeight();
                int miniMapX = guiObject.getX() + width / 2;
                int miniMapY = guiObject.getY() + height / 2;

                float shipX = ship.getX();
                float shipY = ship.getY();
                Vector2f camPos = Engine.renderer.camera.getPosition();
                int x1 = (int) (miniMapX + (shipX - camPos.x) / mapScaleX);
                int y1 = (int) (miniMapY + (shipY - camPos.y) / mapScaleY);

                float r, g, b;
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
                    g = 0.5f;
                    b = 0.5f;
                }

                entityIdToDrawIdMap.put(ship.getId(), new MapEntity(guiRenderer.add(x + x1, y + y1, 4, 4, r, g, b, 1.0f)));
            }
        };
    }

    @EventHandler
    public EventListener<RigidBodyRemovedFromWorldEvent> onShipRemovedFromWorld() {
        return event -> {
            if (event.rigidBody() instanceof Ship ship) {
                guiRenderer.removeObject(entityIdToDrawIdMap.remove(ship.getId()).getRenderId());
            }
        };
    }

    @Override
    public void render() {
        int width = guiObject.getWidth();
        int height = guiObject.getHeight();

        guiRenderer.render();
        List<Ship> ships = client.getWorld().getEntitiesByType(Ship.class);

        Engine.renderer.glEnable(GL.GL_SCISSOR_TEST);
        int offsetY = 17;
        int offsetX = 22;
        Engine.renderer.glScissor(guiObject.getX() + offsetX, Engine.renderer.getScreenHeight() - height + offsetY,
                width - (offsetX << 1), height - (offsetY << 1));

        for (int i = 0; i < ships.size(); i++) {
            MapEntity mapEntity = entityIdToDrawIdMap.get(ships.get(i).getId());
            if (mapEntity.isVisible()) {
                guiRenderer.addDrawCommand(mapEntity.getRenderId(), AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX);
            }
        }

        guiRenderer.render();
        Engine.renderer.glDisable(GL.GL_SCISSOR_TEST);

        super.render();
    }

    @Override
    public void remove() {
        super.remove();
        client.getEventBus().unregister(this);
        entityIdToDrawIdMap.values().forEach((mapEntity) -> guiRenderer.removeObject(mapEntity.getRenderId()));

        entityIdToDrawIdMap.clear();
    }
}
