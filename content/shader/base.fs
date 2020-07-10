#version 330

in vec2 textureCoords0;
out vec4 fragColor;

uniform sampler2D textureOpaque;

uniform vec4 color;
uniform bool useTexture;

void main()
{
	if(useTexture) {
   		fragColor = texture(textureOpaque, textureCoords0) * color;
    } else {
        fragColor = color;
    }
}