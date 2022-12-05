#version 330

layout (location = 0) in vec2 position;
layout (location = 1) in vec2 textureCoords;

out vec2 textureCoords0;

uniform mat4 projMat;
uniform mat4 orthoMat;
uniform mat4 modelViewMat;
uniform vec2 uv_scale;
uniform vec2 uv_offset;

void main()
{
    gl_Position = orthoMat * modelViewMat * vec4(position, 0.0, 1.0);
	textureCoords0 = textureCoords * uv_scale + uv_offset;
}