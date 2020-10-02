#version 330 core

layout (location = 0) in vec4 positionAndTextureCoords;
layout (location = 1) in vec4 inColor;

out vec2 textureCoords0;
out vec4 color;

uniform mat4 orthographicMatrix;
uniform mat4 modelViewMatrix;

void main() {
	gl_Position = orthographicMatrix * modelViewMatrix * (vec4(positionAndTextureCoords.x, positionAndTextureCoords.y, 0.0, 1.0) + vec4(1.0, -1.0, 0.0, 0.0));
	textureCoords0 = vec2(positionAndTextureCoords.z, positionAndTextureCoords.w);
	color = inColor;
}