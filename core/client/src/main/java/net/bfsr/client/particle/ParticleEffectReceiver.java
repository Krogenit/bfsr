package net.bfsr.client.particle;

import net.bfsr.property.ComponentHolder;
import net.bfsr.property.event.PropertyReceiver;

public class ParticleEffectReceiver implements PropertyReceiver<ParticleEffect> {
    @Override
    public boolean canInsert(ComponentHolder<ParticleEffect> componentHolder) {
        return componentHolder.getComponentByType(ParticleEffect.class) != null;
    }

    @Override
    public String getValueForInputBox(ComponentHolder<ParticleEffect> componentHolder) {
        ParticleEffect componentByType = componentHolder.getComponentByType(ParticleEffect.class);
        return componentByType.getPath();
    }
}