#version 330

layout (location = 0) in vec4 positionsUVs;
layout (location = 1) in mat4 modelViewMat;
layout (location = 5) in vec4 texOffsets;
layout (location = 6) in float blendFactor;
layout (location = 7) in vec4 colorIn;

out vec2 textureCoords0;
out vec2 textureCoords1;
out float blend;
out vec4 particleColor;

uniform mat4 orthoMat;
uniform float numberOfRows;

uniform bool animatedTexture;

void main() {
    gl_Position = orthoMat * modelViewMat * vec4(positionsUVs.xy, 0.0, 1.0);
    vec2 textureCoords = positionsUVs.zw;
    particleColor = colorIn;

    if (animatedTexture) {
        vec2 textureCoordsNew = textureCoords / numberOfRows;
        textureCoords0 = textureCoordsNew + texOffsets.xy;
        textureCoords1 = textureCoordsNew + texOffsets.zw;
        blend = blendFactor;
    } else {
        textureCoords0 = textureCoords;
        textureCoords1 = textureCoords;
        blend = 0;
    }
}