#version 330 core

layout (location = 0) in vec4 position;
layout (location = 1) in vec4 inColor;

out vec4 color;

uniform mat4 orthographicMatrix;
uniform mat4 modelViewMatrix;

void main() {
	gl_Position = orthographicMatrix * modelViewMatrix * (vec4(position.x, position.y, 0.0, 1.0) + vec4(1.0, -1.0, 0.0, 0.0));
	color = inColor;
}