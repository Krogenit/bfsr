package net.bfsr.engine.config;

@Configurable
public record ConfigurableSound(String path, float volume, float minPitch, float maxPitch) {}