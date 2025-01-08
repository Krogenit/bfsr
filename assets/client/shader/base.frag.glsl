#version 450
#extension GL_ARB_bindless_texture: enable

layout (location = 0) out vec4 out_Color;

in Data {
    vec2 textureCoords;
    vec4 color;
    flat uvec2 textureHandle;
    flat bool useTexture;
    flat bool useMask;
    flat uvec2 maskTextureHandle;
    flat float fireAmount;
    flat float fireUVAnimation;
    flat bool font;
} in_Data;

layout(binding = 0) uniform sampler2D fireTexture;

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
    if (!in_Data.useTexture) {
        out_Color = in_Data.color;
        return;
    }

    vec4 albedo = texture(sampler2D(in_Data.textureHandle), in_Data.textureCoords);
    if (!in_Data.useMask) {
        if (in_Data.font) {
            out_Color = vec4(in_Data.color.xyz, in_Data.color.a * albedo.r);
        } else {
            out_Color = albedo * in_Data.color;
        }

        return;
    }

    float maskAlpha = texture(sampler2D(in_Data.maskTextureHandle), in_Data.textureCoords).r;
    if (maskAlpha >= 1.0) {
        out_Color = albedo * in_Data.color;
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
    fireMask *= in_Data.fireAmount;

    float fireAmount = texture(fireTexture, rotateUV(in_Data.textureCoords, in_Data.fireUVAnimation)).r;
    vec3 fireColor = vec3(fireAmount * 1.0, fireAmount * 0.45, fireAmount * 0.2);
    out_Color = vec4(albedo.rgb * in_Data.color.rgb * colorMask + fireColor * fireMask, albedo.a * clamp(maskAlpha * 4.0, 0.0, 1.0));
}