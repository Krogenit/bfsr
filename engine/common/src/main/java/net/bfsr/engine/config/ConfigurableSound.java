package net.bfsr.engine.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Configurable
@Accessors(fluent = true)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurableSound {
    private String path;
    private float volume;
    private float minPitch;
    private float maxPitch;
}