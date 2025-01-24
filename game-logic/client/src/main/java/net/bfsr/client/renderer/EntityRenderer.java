package net.bfsr.client.renderer;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.client.Client;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.renderer.component.WeaponRenderRegistry;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.entity.RigidBody;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class EntityRenderer {
    private final AbstractRenderer renderer = Engine.getRenderer();
    private final AbstractCamera camera = renderer.getCamera();

    private final EntityRenderRegistry entityRenderRegistry;
    @Getter
    private final WeaponRenderRegistry weaponRenderRegistry;

    private final List<Render> renders = new ArrayList<>();
    private final TIntObjectMap<Render> rendersMap = new TIntObjectHashMap<>();

    public EntityRenderer(Client client) {
        this.entityRenderRegistry = new EntityRenderRegistry(client);
        this.weaponRenderRegistry = new WeaponRenderRegistry();
        client.getEventBus().register(this);
    }

    public void update() {
        for (int i = 0; i < renders.size(); i++) {
            Render render = renders.get(i);
            if (render.isDead()) {
                render.clear();
                renders.remove(i--);
                rendersMap.remove(render.getObject().getId());
            } else {
                render.update();
            }
        }
    }

    public void postWorldUpdate() {
        for (int i = 0; i < renders.size(); i++) {
            Render render = renders.get(i);
            render.postWorldUpdate();
        }
    }

    public void renderAlpha() {
        if (renderer.isEntitiesGPUFrustumCulling()) {
            for (int i = 0, size = renders.size(); i < size; i++) {
                renders.get(i).renderAlpha();
            }
        } else {
            for (int i = 0, size = renders.size(); i < size; i++) {
                Render render = renders.get(i);
                if (render.getAabb().overlaps(camera.getBoundingBox())) {
                    render.renderAlpha();
                }
            }
        }
    }

    void renderAdditive() {
        if (renderer.isEntitiesGPUFrustumCulling()) {
            for (int i = 0, size = renders.size(); i < size; i++) {
                renders.get(i).renderAdditive();
            }
        } else {
            for (int i = 0, size = renders.size(); i < size; i++) {
                Render render = renders.get(i);
                if (render.getAabb().overlaps(camera.getBoundingBox())) {
                    render.renderAdditive();
                }
            }
        }
    }

    void renderDebug() {
        for (int i = 0; i < renders.size(); i++) {
            Render render = renders.get(i);
            if (render.getAabb().overlaps(camera.getBoundingBox())) {
                render.renderDebug();
            }
        }
    }

    public void createRender(RigidBody rigidBody) {
        addRender(entityRenderRegistry.createRender(rigidBody));
    }

    public void addRender(Render render) {
        renders.add(render);
        rendersMap.put(render.getObject().getId(), render);
    }

    public <T extends Render> T getRender(int id) {
        return (T) rendersMap.get(id);
    }

    public void removeRenderById(int id) {
        renders.remove(rendersMap.remove(id));
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> clear();
    }

    public void clear() {
        for (int i = 0; i < renders.size(); i++) {
            renders.get(i).clear();
        }

        renders.clear();
        rendersMap.clear();
    }
}