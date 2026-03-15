package net.bfsr.engine.config;

import java.util.List;

@Configurable
public record ConfigurableSoundEffect(List<ConfigurableSound> sounds, boolean randomFromList) {}
