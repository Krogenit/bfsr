#version 330 core

layout (location = 0) in vec4 positionAndTextureCoords;
layout (location = 1) in vec4 inColor;

out vec2 textureCoords0;
out vec4 color;

uniform mat4 projection;
uniform mat4 modelView;

void main() {
	gl_Position = projection * modelView * vec4(positionAndTextureCoords.x, positionAndTextureCoords.y, 0.0, 1.0);
	textureCoords0 = vec2(positionAndTextureCoords.z, positionAndTextureCoords.w);
	color = inColor;
}