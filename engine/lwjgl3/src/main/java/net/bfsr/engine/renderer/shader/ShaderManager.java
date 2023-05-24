package net.bfsr.engine.renderer.shader;

import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.renderer.shader.loader.Definition;
import net.bfsr.engine.renderer.shader.loader.FileManager;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.opengl.GL20.glShaderSource;

@Log4j2
public class ShaderManager {
    public static final ShaderManager INSTANCE = new ShaderManager();
    private final FileManager fileManager = new FileManager();
    private final List<ShaderProgram> programs = new ArrayList<>();

    public ShaderProgram createProgram(ShaderProgram program) {
        setupProgram(program);

        for (int i = 0; i < programs.size(); i++) {
            if (programs.get(i).getDefinitions() == null) {
                programs.set(i, program);
                return program;
            }
        }

        programs.add(program);
        return program;
    }

    private boolean setupProgram(ShaderProgram program) {
        program.setProgram(0);

        boolean allFound = true;
        Definition[] definitions = program.getDefinitions();
        for (int i = 0; i < definitions.length; i++) {
            Definition definition = definitions[i];
            String strDefine = switch (definition.getType()) {
                case GL20.GL_VERTEX_SHADER -> "#define _VERTEX_SHADER_ 1\n";
                case GL20.GL_FRAGMENT_SHADER -> "#define _FRAGMENT_SHADER_ 1\n";
                case GL43.GL_COMPUTE_SHADER -> "#define _COMPUTE_SHADER_ 1\n";
                case GL32.GL_GEOMETRY_SHADER -> "#define _GEOMETRY_SHADER_ 1\n";
                case ARBTessellationShader.GL_TESS_CONTROL_SHADER -> "#define _TESS_CONTROL_SHADER_ 1\n";
                case ARBTessellationShader.GL_TESS_EVALUATION_SHADER -> "#define _TESS_EVALUATION_SHADER_ 1\n";
                default -> "";
            };

            definition.setContent(fileManager.manualInclude(definition.getFilename(), definition.getFoundFile(), definition.getPrepend() + strDefine, new AtomicBoolean(false)));

            allFound = allFound && !definition.getContent().isEmpty();
        }

        program.setProgram(GL20.glCreateProgram());

        String lastFileName = "";
        for (int i = 0; i < definitions.length; i++) {
            Definition definition = definitions[i];
            if (!definition.getContent().isEmpty()) {
                String content = definition.getContent();
                definition.setShader(GL20.glCreateShader(definition.getType()));
                glShaderSource(definition.getShader(), content);
                GL20.glCompileShader(definition.getShader());
            }
            if (definition.getShader() == 0 || !checkShader(definition.getShader(), definition.getFilename())) {
                GL20.glDeleteShader(definition.getShader());
                GL20.glDeleteProgram(program.getProgram());
                program.setProgram(0);
                return false;
            }
            GL20.glAttachShader(program.getProgram(), definition.getShader());
            GL20.glDeleteShader(definition.getShader());
            lastFileName = definition.getFilename();
        }
        GL20.glLinkProgram(program.getProgram());

        if (checkProgram(program.getProgram(), lastFileName)) {
            return true;
        }

        GL20.glDeleteProgram(program.getProgram());
        program.setProgram(0);
        return false;
    }

    private boolean checkProgram(int program, String filename) {
        if (program == 0) return false;
        int linkResult = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);
        int infoLogLength = GL20.glGetProgrami(program, GL20.GL_INFO_LOG_LENGTH);
        if (infoLogLength > 1) {
            String infoLog = GL20.glGetProgramInfoLog(program, GL20.GL_INFO_LOG_LENGTH);
            if (linkResult == GL11.GL_FALSE) log.error("Could not link program {}, log: {}", filename, infoLog);
            else log.info("Program {} link log: {}", filename, infoLog);
        }

        GL20.glValidateProgram(program);
        int validateResult = GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS);
        infoLogLength = GL20.glGetProgrami(program, GL20.GL_INFO_LOG_LENGTH);
        if (infoLogLength > 1) {
            String infoLog = GL20.glGetProgramInfoLog(program, GL20.GL_INFO_LOG_LENGTH);
            if (validateResult == GL11.GL_FALSE) log.error("Could not validate program {}, log: {}", filename, infoLog);
            else log.info("Program {} validate log: {}", filename, infoLog);
        }

        return linkResult == GL11.GL_TRUE && validateResult == GL11.GL_TRUE;
    }

    private boolean checkShader(int shader, String filename) {
        if (shader == 0) return false;
        int result = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS);
        int infoLogLength = GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH);
        if (infoLogLength > 1) {
            String infoLog = GL20.glGetShaderInfoLog(shader, infoLogLength);
            if (result == GL11.GL_FALSE) log.error("Could not compile shader {}, log: {}", filename, infoLog);
            else log.info("Shader {} compile log: {}", filename, infoLog);
        }

        return result == GL11.GL_TRUE;
    }
}