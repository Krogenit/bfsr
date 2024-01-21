package net.bfsr.client.renderer;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.client.Core;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.renderer.component.WeaponRenderRegistry;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.entity.RigidBody;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RenderManager {
    private final AbstractCamera camera = Engine.renderer.camera;

    private final RenderRegistry renderRegistry = new RenderRegistry();
    @Getter
    private final WeaponRenderRegistry weaponRenderRegistry = new WeaponRenderRegistry();

    private final List<Render<?>> renderList = new ArrayList<>();
    private final TIntObjectMap<Render<?>> renders = new TIntObjectHashMap<>();

    public void init() {
        Core.get().getEventBus().register(this);
    }

    public void update() {
        for (int i = 0; i < renderList.size(); i++) {
            Render<?> render = renderList.get(i);
            if (render.isDead()) {
                render.clear();
                renderList.remove(i--);
                renders.remove(render.getObject().getId());
            } else {
                render.update();
            }
        }
    }

    public void postWorldUpdate() {
        for (int i = 0; i < renderList.size(); i++) {
            Render<?> render = renderList.get(i);
            render.postWorldUpdate();
        }
    }

    public void renderAlpha() {
        for (int i = 0; i < renderList.size(); i++) {
            Render<?> render = renderList.get(i);
            if (render.getAabb().overlaps(camera.getBoundingBox())) {
                render.renderAlpha();
            }
        }
    }

    void renderAdditive() {
        for (int i = 0; i < renderList.size(); i++) {
            Render<?> render = renderList.get(i);
            if (render.getAabb().overlaps(camera.getBoundingBox())) {
                render.renderAdditive();
            }
        }
    }

    void renderDebug() {
        for (int i = 0; i < renderList.size(); i++) {
            Render<?> render = renderList.get(i);
            if (render.getAabb().overlaps(camera.getBoundingBox())) {
                render.renderDebug();
            }
        }
    }

    public void createRender(RigidBody<? extends GameObjectConfigData> rigidBody) {
        addRender(renderRegistry.createRender(rigidBody));
    }

    public void addRender(Render<?> render) {
        renderList.add(render);
        renders.put(render.getObject().getId(), render);
    }

    public <T extends Render<?>> T getRender(int id) {
        return (T) renders.get(id);
    }

    public void removeRenderById(int id) {
        renderList.remove(renders.remove(id));
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> clear();
    }

    public void clear() {
        for (int i = 0; i < renderList.size(); i++) {
            renderList.get(i).clear();
        }

        renderList.clear();
        renders.clear();
    }
}