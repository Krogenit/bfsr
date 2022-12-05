#version 330

layout (location = 0) out vec4 fragColor;
in vec2 textureCoords0;

uniform sampler2D textureOpaque;
uniform vec3 coreColor, haloColor;
uniform vec2 center, resolution;
uniform float coreRadius, haloFalloff, scale;

void main()
{
	vec3 s = texture(textureOpaque, textureCoords0).rgb;
	float d = length(gl_FragCoord.xy - center * resolution) / scale;
	if (d <= coreRadius) {
		fragColor = vec4(coreColor, 1);
		return;
	}
	
	float e = 1.0 - exp(-(d - coreRadius) * haloFalloff);
	vec3 rgb = mix(coreColor, haloColor, e);
	rgb = mix(rgb, vec3(0,0,0), e);
	fragColor = vec4(rgb + s, 1);
}