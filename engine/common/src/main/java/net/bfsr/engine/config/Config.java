package net.bfsr.engine.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Config {
    private String name;
    private String path;
    private int treeIndex;

    public String getFullPath() {
        return path.isEmpty() ? name : path + "/" + name;
    }
}