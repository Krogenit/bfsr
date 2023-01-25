#version 330

out vec4 fragColor;

in vec2 textureCoords0;
in vec2 textureCoords1;
in float blend;
in vec4 particleColor;

uniform sampler2D textureOpaque;

uniform bool useTexture;
uniform bool animatedTexture;

void main() {
    if (useTexture) {
        if (animatedTexture) {
            vec4 color1 = texture(textureOpaque, textureCoords0);
            vec4 color2 = texture(textureOpaque, textureCoords1);

            fragColor = mix(color1, color2, blend) * particleColor;
        } else {
            fragColor = texture(textureOpaque, textureCoords0) * particleColor;
        }
    } else {
        fragColor = particleColor;
    }
}