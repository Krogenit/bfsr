#version 330

layout (location = 0) in vec4 positionsUVs;

out vec2 textureCoords0;

void main()
{
    gl_Position = vec4(positionsUVs.xy, 0.0, 0.5);
    textureCoords0 = positionsUVs.zw;
}