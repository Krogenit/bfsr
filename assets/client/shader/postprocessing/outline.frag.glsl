#version 460

layout (location = 0) in vec2 textureCoords;

layout (binding = 0) uniform sampler2D image;

uniform float offset;

layout (location = 0) out vec4 out_Color;

void main() {
    vec4 col = texture(image, textureCoords);
    float a = texture(image, vec2(textureCoords.x + offset, textureCoords.y)).a +
    texture(image, vec2(textureCoords.x, textureCoords.y - offset)).a +
    texture(image, vec2(textureCoords.x - offset, textureCoords.y)).a +
    texture(image, vec2(textureCoords.x, textureCoords.y + offset)).a;
    if (col.a < 1.0 && a > 0.0) {
        out_Color = vec4(col.rgb, a);
    } else {
        out_Color = col;
    }
}