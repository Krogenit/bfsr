#version 330 core

in vec2 textureCoords0;
in vec4 color;

out vec4 fragColor;

uniform sampler2D fontAtlas;

void main() {
	fragColor = vec4(color.rgb, texture(fontAtlas, textureCoords0).a * color.a);
}