package net.bfsr.editor.property.converter;

import net.bfsr.engine.renderer.particle.RenderLayer;

public class RenderLayerConverter implements PropertyConverter<RenderLayer> {
    @Override
    public String toString(RenderLayer value) {
        return value.toString();
    }

    @Override
    public RenderLayer fromString(String value) {
        return RenderLayer.valueOf(value);
    }
}