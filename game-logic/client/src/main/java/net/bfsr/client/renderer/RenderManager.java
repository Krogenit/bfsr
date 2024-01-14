package net.bfsr.client.renderer;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.RequiredArgsConstructor;
import net.bfsr.client.Core;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.entity.RigidBody;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Listener
public class RenderManager {
    private final AbstractCamera camera = Engine.renderer.camera;

    private final RenderRegistry renderRegistry = new RenderRegistry();

    private final List<Render<?>> renderList = new ArrayList<>();
    private final TIntObjectMap<Render<?>> renders = new TIntObjectHashMap<>();

    public void init() {
        Core.get().subscribe(this);
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

    @Handler
    public void event(ExitToMainMenuEvent event) {
        clear();
    }

    public void clear() {
        for (int i = 0; i < renderList.size(); i++) {
            renderList.get(i).clear();
        }

        renderList.clear();
        renders.clear();
    }
}