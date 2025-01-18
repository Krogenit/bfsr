#version 450
#extension GL_ARB_bindless_texture: enable

layout (location = 0) in data {
    vec2 textureCoords;
    vec4 color;
    flat uvec2 textureHandle;
    flat bool useTexture;
    flat bool useMask;
    flat uvec2 maskTextureHandle;
    flat float fireAmount;
    flat float fireUVAnimation;
    flat bool font;
};

layout (location = 0) out vec4 out_Color;

layout (binding = 0) uniform sampler2D fireTexture;

vec2 rotateUV(vec2 uv, float rotation) {
    float mid = 0.5;
    float cosAngle = cos(rotation);
    float sinAngle = sin(rotation);
    return vec2(
    cosAngle * (uv.x - mid) + sinAngle * (uv.y - mid) + mid,
    cosAngle * (uv.y - mid) - sinAngle * (uv.x - mid) + mid
    );
}

void main() {
    if (!useTexture) {
        out_Color = color;
        return;
    }

    vec4 albedo = texture(sampler2D(textureHandle), textureCoords);
    if (!useMask) {
        if (font) {
            out_Color = vec4(color.xyz, color.a * albedo.r);
        } else {
            out_Color = albedo * color;
        }

        return;
    }

    float maskAlpha = texture(sampler2D(maskTextureHandle), textureCoords).r;
    if (maskAlpha >= 1.0) {
        out_Color = albedo * color;
        return;
    }

    if (maskAlpha <= 0.0) {
        discard;
    }

    float colorMask = maskAlpha < 0.85 ? clamp(pow(maskAlpha, 4), 0.0, 1.0) : maskAlpha;
    float invertexMaskAlpha = 1.0 - maskAlpha;
    float fireMask = 0.0;
    if (maskAlpha < 0.85) {
        if (maskAlpha > 0.6) {
            fireMask = clamp(pow(1.0 - (maskAlpha - 0.6) * 4.0, 1), 0.0, 1.0);
        } else if (maskAlpha > 0.3) {
            fireMask = clamp(pow((maskAlpha - 0.3) * 3.33, 1), 0.0, 1.0);
        }
    }
    fireMask *= fireAmount;

    float fireAmount = texture(fireTexture, rotateUV(textureCoords, fireUVAnimation)).r;
    vec3 fireColor = vec3(fireAmount * 1.0, fireAmount * 0.45, fireAmount * 0.2);
    out_Color = vec4(albedo.rgb * color.rgb * colorMask + fireColor * fireMask, albedo.a * clamp(maskAlpha * 4.0, 0.0, 1.0));
}