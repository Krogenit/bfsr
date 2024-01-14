package net.bfsr.engine.renderer.shader;

import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.renderer.shader.loader.Definition;
import net.bfsr.engine.renderer.shader.loader.FileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.opengl.GL45C.*;

@Log4j2
class ShaderManager {
    static final ShaderManager INSTANCE = new ShaderManager();
    private final FileManager fileManager = new FileManager();
    private final List<ShaderProgram> programs = new ArrayList<>();

    void createProgram(ShaderProgram program) {
        setupProgram(program);

        for (int i = 0; i < programs.size(); i++) {
            if (programs.get(i).getDefinitions() == null) {
                programs.set(i, program);
                return;
            }
        }

        programs.add(program);
    }

    private void setupProgram(ShaderProgram program) {
        program.setProgram(0);

        boolean allFound = true;
        Definition[] definitions = program.getDefinitions();
        for (int i = 0; i < definitions.length; i++) {
            Definition definition = definitions[i];
            String strDefine = switch (definition.getType()) {
                case GL_VERTEX_SHADER -> "#define _VERTEX_SHADER_ 1\n";
                case GL_FRAGMENT_SHADER -> "#define _FRAGMENT_SHADER_ 1\n";
                case GL_COMPUTE_SHADER -> "#define _COMPUTE_SHADER_ 1\n";
                case GL_GEOMETRY_SHADER -> "#define _GEOMETRY_SHADER_ 1\n";
                case GL_TESS_CONTROL_SHADER -> "#define _TESS_CONTROL_SHADER_ 1\n";
                case GL_TESS_EVALUATION_SHADER -> "#define _TESS_EVALUATION_SHADER_ 1\n";
                default -> "";
            };

            definition.setContent(fileManager.manualInclude(definition.getFilename(), definition.getFoundFile(),
                    definition.getPrepend() + strDefine, new AtomicBoolean(false)));

            allFound = allFound && !definition.getContent().isEmpty();
        }

        program.setProgram(glCreateProgram());

        String lastFileName = "";
        for (int i = 0; i < definitions.length; i++) {
            Definition definition = definitions[i];
            if (!definition.getContent().isEmpty()) {
                String content = definition.getContent();
                definition.setShader(glCreateShader(definition.getType()));
                glShaderSource(definition.getShader(), content);
                glCompileShader(definition.getShader());
            }
            if (definition.getShader() == 0 || !checkShader(definition.getShader(), definition.getFilename())) {
                glDeleteShader(definition.getShader());
                glDeleteProgram(program.getProgram());
                program.setProgram(0);
                return;
            }
            glAttachShader(program.getProgram(), definition.getShader());
            glDeleteShader(definition.getShader());
            lastFileName = definition.getFilename();
        }
        glLinkProgram(program.getProgram());

        if (checkProgram(program.getProgram(), lastFileName)) {
            return;
        }

        glDeleteProgram(program.getProgram());
        program.setProgram(0);
    }

    private boolean checkProgram(int program, String filename) {
        if (program == 0) return false;
        int linkResult = glGetProgrami(program, GL_LINK_STATUS);
        int infoLogLength = glGetProgrami(program, GL_INFO_LOG_LENGTH);
        if (infoLogLength > 1) {
            String infoLog = glGetProgramInfoLog(program, GL_INFO_LOG_LENGTH);
            if (linkResult == GL_FALSE) log.error("Could not link program {}, log: {}", filename, infoLog);
            else log.info("Program {} link log: {}", filename, infoLog);
        }

        glValidateProgram(program);
        int validateResult = glGetProgrami(program, GL_VALIDATE_STATUS);
        infoLogLength = glGetProgrami(program, GL_INFO_LOG_LENGTH);
        if (infoLogLength > 1) {
            String infoLog = glGetProgramInfoLog(program, GL_INFO_LOG_LENGTH);
            if (validateResult == GL_FALSE) log.error("Could not validate program {}, log: {}", filename, infoLog);
            else log.info("Program {} validate log: {}", filename, infoLog);
        }

        return linkResult == GL_TRUE && validateResult == GL_TRUE;
    }

    private boolean checkShader(int shader, String filename) {
        if (shader == 0) return false;
        int result = glGetShaderi(shader, GL_COMPILE_STATUS);
        int infoLogLength = glGetShaderi(shader, GL_INFO_LOG_LENGTH);
        if (infoLogLength > 1) {
            String infoLog = glGetShaderInfoLog(shader, infoLogLength);
            if (result == GL_FALSE) log.error("Could not compile shader {}, log: {}", filename, infoLog);
            else log.info("Shader {} compile log: {}", filename, infoLog);
        }

        return result == GL_TRUE;
    }
}