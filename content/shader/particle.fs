#version 330

in vec2 textureCoords0;
in vec2 textureCoords1;
in float blend;
out vec4 fragColor;

uniform sampler2D textureOpaque;

uniform vec4 color;
uniform bool useTexture;

uniform bool animatedTexture;

void main()
{
	if(useTexture) {
		if(animatedTexture) {
			vec4 color1 = texture(textureOpaque, textureCoords0);
			vec4 color2 = texture(textureOpaque, textureCoords1);
			
			fragColor = mix(color1, color2, blend) * color; 
		} else {
		   	fragColor = texture(textureOpaque, textureCoords0) * color;
		}
    } else {
        fragColor = color;
    }
}