#version 460

layout (location = 0) in vec2 textureCoords0;

layout (binding = 0) uniform sampler2D image;

uniform vec2 resolution;
uniform float size; // Blur size (radius)

layout (location = 0) out vec4 out_Color;

const float twoPi = 6.28318530718;
const float directions = 12.0; // Blur directions (default 16.0 - more is better but slower)
const float quality = 3.0; // Blur quality (default 4.0 - more is better but slower)

void main() {
    vec2 radius = size / resolution;

    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = gl_FragCoord.xy / resolution;
    vec4 color = texture(image, uv);

    // Blur calculations
    for (float d = 0.0; d < twoPi; d += twoPi / directions) {
        for (float i = 1.0 / quality; i <= 1.0; i += 1.0 / quality) {
            color += texture(image, uv + vec2(cos(d), sin(d)) * radius * i);
        }
    }

    // Output to screen
    color /= quality * directions - 15.0;
    out_Color = color;
}