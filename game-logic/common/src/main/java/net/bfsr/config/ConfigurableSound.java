package net.bfsr.config;

import lombok.Getter;

@Configurable
@Getter
public class ConfigurableSound extends NameableConfig {
    private String path;
    private float volume;
}