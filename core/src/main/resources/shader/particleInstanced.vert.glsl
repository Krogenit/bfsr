#version 330

layout (location = 0) in vec4 positionsUVs;
layout (location = 1) in mat4 modelViewMat;
layout (location = 5) in vec4 colorIn;

out vec2 textureCoords;
out vec2 textureCoords1;
out vec4 particleColor;

uniform mat4 orthoMat;

void main() {
    gl_Position = orthoMat * modelViewMat * vec4(positionsUVs.xy, 0.0, 1.0);
    particleColor = colorIn;
    textureCoords = positionsUVs.zw;
}