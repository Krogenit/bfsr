struct Material {
    vec4 color;
    uvec2 textureHandle;
    bool useTexture;
    bool useMask;
    uvec2 maskTextureHandle;
    float fireAmount;
    float fireUVAnimation;
    bool font;
    bool padding0;
    bool padding1;
    bool padding2;
};

#define UBO_CAMERA_MATRIX 0