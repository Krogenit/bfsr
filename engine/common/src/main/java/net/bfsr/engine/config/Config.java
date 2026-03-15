package net.bfsr.engine.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Config {
    private String name;
    private String path;
    private int treeIndex;

    public String getFullPath() {
        return path.isEmpty() ? name : path + "/" + name;
    }
}