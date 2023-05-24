package net.bfsr.client.renderer;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.camera.AbstractCamera;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RenderManager {
    private final AbstractCamera camera = Engine.renderer.camera;

    private final List<Render<?>> renderList = new ArrayList<>();
    private final TIntObjectMap<Render<?>> renders = new TIntObjectHashMap<>();

    public void update() {
        if (!Engine.isPaused()) {
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
    }

    public void postUpdate() {
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

    public void renderAdditive() {
        for (int i = 0; i < renderList.size(); i++) {
            Render<?> render = renderList.get(i);
            if (render.getAabb().overlaps(camera.getBoundingBox())) {
                render.renderAdditive();
            }
        }
    }

    public void renderDebug() {
        for (int i = 0; i < renderList.size(); i++) {
            Render<?> render = renderList.get(i);
            if (render.getAabb().overlaps(camera.getBoundingBox())) {
                render.renderDebug();
            }
        }
    }

    public void addRender(Render<?> render) {
        renderList.add(render);
        renders.put(render.getObject().getId(), render);
    }

    public <T extends Render<?>> T getRender(int id) {
        return (T) renders.get(id);
    }

    public void clear() {
        for (int i = 0; i < renderList.size(); i++) {
            renderList.get(i).clear();
        }

        renderList.clear();
        renders.clear();
    }
}