#version 330 core

in vec4 color;

out vec4 fragColor;

void main() {
	fragColor = vec4(color.rgb, color.a);
}