package net.bfsr.config;

@Configurable
public record ConfigurableSound(
        String path,
        float volume
) {}