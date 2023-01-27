#version 330

out vec4 fragColor;

in vec2 textureCoords;
in vec4 particleColor;

uniform sampler2D textureOpaque;

void main() {
    fragColor = texture(textureOpaque, textureCoords) * particleColor;
}