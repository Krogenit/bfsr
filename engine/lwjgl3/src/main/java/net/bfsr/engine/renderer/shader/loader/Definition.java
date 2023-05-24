package net.bfsr.engine.renderer.shader.loader;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Definition {
    private final int type;
    private final String filename;
    private final String prepend;
    private String content;
    private FoundFile foundFile = new FoundFile();
    private int shader;

    public Definition(int type, String prepend, String filename) {
        this.type = type;
        this.filename = filename;
        this.prepend = prepend;
    }

    public Definition(int type, String filename) {
        this(type, "", filename);
    }
}