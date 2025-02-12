#version 460

layout (location = 0) in vec4 positionsUV;

layout (location = 0) out vec2 textureCoords0;

void main() {
    gl_Position = vec4(positionsUV.xy, 0.0, 0.5);
    textureCoords0 = positionsUV.zw;
}