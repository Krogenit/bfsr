#version 330

layout (location =0) in vec2 position;
layout (location =1) in vec2 textureCoords;

out vec2 textureCoords0;

uniform vec2 translation;
uniform mat4 orthoMat;
uniform mat4 modelViewMat;

void main()
{
	gl_Position = orthoMat * modelViewMat * vec4(position, 0.0, 1.0);
	textureCoords0 = textureCoords;
}