#version 330

layout (location = 0) out vec4 fragColor;
in vec2 textureCoords0;

uniform sampler2D textureOpaque, textureNoise;

uniform vec3 color;
uniform vec2 offset;
uniform float scale, density, falloff, textureNoiseSize;
uniform int noiseType;
uniform vec4 pNoiseRepeatVector;

vec3 mod289(vec3 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec2 mod289(vec2 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec3 permute(vec3 x) {
    return mod289(((x*34.0)+1.0)*x);
}

float snoise(vec2 v)
{
    const vec4 C = vec4(0.211324865405187, // (3.0-sqrt(3.0))/6.0
    0.366025403784439, // 0.5*(sqrt(3.0)-1.0)
    -0.577350269189626, // -1.0 + 2.0 * C.x
    0.024390243902439);// 1.0 / 41.0
    // First corner
    vec2 i  = floor(v + dot(v, C.yy));
    vec2 x0 = v -   i + dot(i, C.xx);

    // Other corners
    vec2 i1;
    //i1.x = step( x0.y, x0.x ); // x0.x > x0.y ? 1.0 : 0.0
    //i1.y = 1.0 - i1.x;
    i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    // x0 = x0 - 0.0 + 0.0 * C.xx ;
    // x1 = x0 - i1 + 1.0 * C.xx ;
    // x2 = x0 - 1.0 + 2.0 * C.xx ;
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;

    // Permutations
    i = mod289(i);// Avoid truncation effects in permutation
    vec3 p = permute(permute(i.y + vec3(0.0, i1.y, 1.0))
    + i.x + vec3(0.0, i1.x, 1.0));

    vec3 m = max(0.5 - vec3(dot(x0, x0), dot(x12.xy, x12.xy), dot(x12.zw, x12.zw)), 0.0);
    m = m*m;
    m = m*m;

    // Gradients: 41 points uniformly over a line, mapped onto a diamond.
    // The ring size 17*17 = 289 is close to a multiple of 41 (41*7 = 287)

    vec3 x = 2.0 * fract(p * C.www) - 1.0;
    vec3 h = abs(x) - 0.5;
    vec3 ox = floor(x + 0.5);
    vec3 a0 = x - ox;

    // Normalise gradients implicitly by scaling m
    // Approximation of: m *= inversesqrt( a0*a0 + h*h );
    m *= 1.79284291400159 - 0.85373472095314 * (a0*a0 + h*h);

    // Compute final noise value at P
    vec3 g;
    g.x  = a0.x  * x0.x  + h.x  * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;
    return pNoiseRepeatVector.x * dot(m, g);
}

vec4 mod289(vec4 x)
{
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x)
{
    return mod289(((x*34.0)+1.0)*x);
}

vec4 taylorInvSqrt(vec4 r)
{
    return 1.79284291400159 - 0.85373472095314 * r;
}

vec2 fade(vec2 t) {
    return t*t*t*(t*(t*6.0-15.0)+10.0);
}

// Classic Perlin noise
float cnoise(vec2 P)
{
    vec4 Pi = floor(P.xyxy) + vec4(0.0, 0.0, 1.0, 1.0);
    vec4 Pf = fract(P.xyxy) - vec4(0.0, 0.0, 1.0, 1.0);
    Pi = mod289(Pi);// To avoid truncation effects in permutation
    vec4 ix = Pi.xzxz;
    vec4 iy = Pi.yyww;
    vec4 fx = Pf.xzxz;
    vec4 fy = Pf.yyww;

    vec4 i = permute(permute(ix) + iy);

    vec4 gx = fract(i * (1.0 / 41.0)) * 2.0 - 1.0;
    vec4 gy = abs(gx) - 0.5;
    vec4 tx = floor(gx + 0.5);
    gx = gx - tx;

    vec2 g00 = vec2(gx.x, gy.x);
    vec2 g10 = vec2(gx.y, gy.y);
    vec2 g01 = vec2(gx.z, gy.z);
    vec2 g11 = vec2(gx.w, gy.w);

    vec4 norm = taylorInvSqrt(vec4(dot(g00, g00), dot(g01, g01), dot(g10, g10), dot(g11, g11)));
    g00 *= norm.x;
    g01 *= norm.y;
    g10 *= norm.z;
    g11 *= norm.w;

    float n00 = dot(g00, vec2(fx.x, fy.x));
    float n10 = dot(g10, vec2(fx.y, fy.y));
    float n01 = dot(g01, vec2(fx.z, fy.z));
    float n11 = dot(g11, vec2(fx.w, fy.w));

    vec2 fade_xy = fade(Pf.xy);
    vec2 n_x = mix(vec2(n00, n01), vec2(n10, n11), fade_xy.x);
    float n_xy = mix(n_x.x, n_x.y, fade_xy.y);
    return pNoiseRepeatVector.x * n_xy;
}

// Classic Perlin noise, periodic variant
float pnoise(vec2 P, vec2 rep)
{
    vec4 Pi = floor(P.xyxy) + vec4(0.0, 0.0, 1.0, 1.0);
    vec4 Pf = fract(P.xyxy) - vec4(0.0, 0.0, 1.0, 1.0);
    Pi = mod(Pi, rep.xyxy);// To create noise with explicit period
    Pi = mod289(Pi);// To avoid truncation effects in permutation
    vec4 ix = Pi.xzxz;
    vec4 iy = Pi.yyww;
    vec4 fx = Pf.xzxz;
    vec4 fy = Pf.yyww;

    vec4 i = permute(permute(ix) + iy);

    vec4 gx = fract(i * (1.0 / 41.0)) * 2.0 - 1.0;
    vec4 gy = abs(gx) - 0.5;
    vec4 tx = floor(gx + 0.5);
    gx = gx - tx;

    vec2 g00 = vec2(gx.x, gy.x);
    vec2 g10 = vec2(gx.y, gy.y);
    vec2 g01 = vec2(gx.z, gy.z);
    vec2 g11 = vec2(gx.w, gy.w);

    vec4 norm = taylorInvSqrt(vec4(dot(g00, g00), dot(g01, g01), dot(g10, g10), dot(g11, g11)));
    g00 *= norm.x;
    g01 *= norm.y;
    g10 *= norm.z;
    g11 *= norm.w;

    float n00 = dot(g00, vec2(fx.x, fy.x));
    float n10 = dot(g10, vec2(fx.y, fy.y));
    float n01 = dot(g01, vec2(fx.z, fy.z));
    float n11 = dot(g11, vec2(fx.w, fy.w));

    vec2 fade_xy = fade(Pf.xy);
    vec2 n_x = mix(vec2(n00, n01), vec2(n10, n11), fade_xy.x);
    float n_xy = mix(n_x.x, n_x.y, fade_xy.y);
    return pNoiseRepeatVector.x * n_xy;
}

float smootherstep(float a, float b, float r) {
    r = clamp(r, 0.0, 1.0);
    r = r * r * r * (r * (6.0 * r - 15.0) + 10.0);
    return mix(a, b, r);
}

float perlin_2d(vec2 p) {
    vec2 p0 = floor(p);
    vec2 p1 = p0 + vec2(1, 0);
    vec2 p2 = p0 + vec2(1, 1);
    vec2 p3 = p0 + vec2(0, 1);
    vec2 d0 = texture(textureNoise, p0/textureNoiseSize).ba;
    vec2 d1 = texture(textureNoise, p1/textureNoiseSize).ba;
    vec2 d2 = texture(textureNoise, p2/textureNoiseSize).ba;
    vec2 d3 = texture(textureNoise, p3/textureNoiseSize).ba;
    d0 = 2.0 * d0 - 1.0;
    d1 = 2.0 * d1 - 1.0;
    d2 = 2.0 * d2 - 1.0;
    d3 = 2.0 * d3 - 1.0;
    vec2 p0p = p - p0;
    vec2 p1p = p - p1;
    vec2 p2p = p - p2;
    vec2 p3p = p - p3;
    float dp0 = dot(d0, p0p);
    float dp1 = dot(d1, p1p);
    float dp2 = dot(d2, p2p);
    float dp3 = dot(d3, p3p);
    float fx = p.x - p0.x;
    float fy = p.y - p0.y;
    float m01 = smootherstep(dp0, dp1, fx);
    float m32 = smootherstep(dp3, dp2, fx);
    float m01m32 = smootherstep(m01, m32, fy);
    return m01m32;
}

float mod289(float x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

// Permutation polynomial (ring size 289 = 17*17)
float permute(float x) {
    return mod289(((x*34.0)+1.0)*x);
}

// Hashed 2-D gradients with an extra rotation.
// (The constant 0.0243902439 is 1/41)
vec2 rgrad2(vec2 p, float rot) {
    #if 0
    // Map from a line to a diamond such that a shift maps to a rotation.
    float u = permute(permute(p.x) + p.y) * 0.0243902439 + rot;// Rotate by shift
    u = 4.0 * fract(u) - 2.0;
    // (This vector could be normalized, exactly or approximately.)
    return vec2(abs(u)-1.0, abs(abs(u+1.0)-2.0)-1.0);
    #else
    // For more isotropic gradients, sin/cos can be used instead.
    float u = permute(permute(p.x) + p.y) * 0.0243902439 + rot;// Rotate by shift
    u = fract(u) * 6.28318530718;// 2*pi
    return vec2(cos(u), sin(u));
    #endif
}

//
// 2-D tiling simplex noise with rotating gradients and analytical derivative.
// The first component of the 3-element return vector is the noise value,
// and the second and third components are the x and y partial derivatives.
//
vec3 psrdnoise(vec2 pos, vec2 per, float rot) {
    // Hack: offset y slightly to hide some rare artifacts
    pos.y += 0.01;
    // Skew to hexagonal grid
    vec2 uv = vec2(pos.x + pos.y*0.5, pos.y);

    vec2 i0 = floor(uv);
    vec2 f0 = fract(uv);
    // Traversal order
    vec2 i1 = (f0.x > f0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);

    // Unskewed grid points in (x,y) space
    vec2 p0 = vec2(i0.x - i0.y * 0.5, i0.y);
    vec2 p1 = vec2(p0.x + i1.x - i1.y * 0.5, p0.y + i1.y);
    vec2 p2 = vec2(p0.x + 0.5, p0.y + 1.0);

    // Integer grid point indices in (u,v) space
    i1 = i0 + i1;
    vec2 i2 = i0 + vec2(1.0, 1.0);

    // Vectors in unskewed (x,y) coordinates from
    // each of the simplex corners to the evaluation point
    vec2 d0 = pos - p0;
    vec2 d1 = pos - p1;
    vec2 d2 = pos - p2;

    // Wrap i0, i1 and i2 to the desired period before gradient hashing:
    // wrap points in (x,y), map to (u,v)
    vec3 xw = mod(vec3(p0.x, p1.x, p2.x), per.x);
    vec3 yw = mod(vec3(p0.y, p1.y, p2.y), per.y);
    vec3 iuw = xw + 0.5 * yw;
    vec3 ivw = yw;

    // Create gradients from indices
    vec2 g0 = rgrad2(vec2(iuw.x, ivw.x), rot);
    vec2 g1 = rgrad2(vec2(iuw.y, ivw.y), rot);
    vec2 g2 = rgrad2(vec2(iuw.z, ivw.z), rot);

    // Gradients dot vectors to corresponding corners
    // (The derivatives of this are simply the gradients)
    vec3 w = vec3(dot(g0, d0), dot(g1, d1), dot(g2, d2));

    // Radial weights from corners
    // 0.8 is the square of 2/sqrt(5), the distance from
    // a grid point to the nearest simplex boundary
    vec3 t = 0.8 - vec3(dot(d0, d0), dot(d1, d1), dot(d2, d2));

    // Partial derivatives for analytical gradient computation
    vec3 dtdx = -2.0 * vec3(d0.x, d1.x, d2.x);
    vec3 dtdy = -2.0 * vec3(d0.y, d1.y, d2.y);

    // Set influence of each surflet to zero outside radius sqrt(0.8)
    if (t.x < 0.0) {
        dtdx.x = 0.0;
        dtdy.x = 0.0;
        t.x = 0.0;
    }
    if (t.y < 0.0) {
        dtdx.y = 0.0;
        dtdy.y = 0.0;
        t.y = 0.0;
    }
    if (t.z < 0.0) {
        dtdx.z = 0.0;
        dtdy.z = 0.0;
        t.z = 0.0;
    }

    // Fourth power of t (and third power for derivative)
    vec3 t2 = t * t;
    vec3 t4 = t2 * t2;
    vec3 t3 = t2 * t;

    // Final noise value is:
    // sum of ((radial weights) times (gradient dot vector from corner))
    float n = dot(t4, w);

    // Final analytical derivative (gradient of a sum of scalar products)
    vec2 dt0 = vec2(dtdx.x, dtdy.x) * 4.0 * t3.x;
    vec2 dn0 = t4.x * g0 + dt0 * w.x;
    vec2 dt1 = vec2(dtdx.y, dtdy.y) * 4.0 * t3.y;
    vec2 dn1 = t4.y * g1 + dt1 * w.y;
    vec2 dt2 = vec2(dtdx.z, dtdy.z) * 4.0 * t3.z;
    vec2 dn2 = t4.z * g2 + dt2 * w.z;

    return pNoiseRepeatVector.x*vec3(n, dn0 + dn1 + dn2);
}

//
// 2-D tiling simplex noise with fixed gradients
// and analytical derivative.
// This function is implemented as a wrapper to "psrdnoise",
// at the minimal cost of three extra additions.
//
vec3 psdnoise(vec2 pos, vec2 per) {
    return psrdnoise(pos, per, 0.0);
}

//
// 2-D tiling simplex noise with rotating gradients,
// but without the analytical derivative.
//
float psrnoise(vec2 pos, vec2 per, float rot) {
    // Offset y slightly to hide some rare artifacts
    pos.y += 0.001;
    // Skew to hexagonal grid
    vec2 uv = vec2(pos.x + pos.y*0.5, pos.y);

    vec2 i0 = floor(uv);
    vec2 f0 = fract(uv);
    // Traversal order
    vec2 i1 = (f0.x > f0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);

    // Unskewed grid points in (x,y) space
    vec2 p0 = vec2(i0.x - i0.y * 0.5, i0.y);
    vec2 p1 = vec2(p0.x + i1.x - i1.y * 0.5, p0.y + i1.y);
    vec2 p2 = vec2(p0.x + 0.5, p0.y + 1.0);

    // Integer grid point indices in (u,v) space
    i1 = i0 + i1;
    vec2 i2 = i0 + vec2(1.0, 1.0);

    // Vectors in unskewed (x,y) coordinates from
    // each of the simplex corners to the evaluation point
    vec2 d0 = pos - p0;
    vec2 d1 = pos - p1;
    vec2 d2 = pos - p2;

    // Wrap i0, i1 and i2 to the desired period before gradient hashing:
    // wrap points in (x,y), map to (u,v)
    vec3 xw = mod(vec3(p0.x, p1.x, p2.x), per.x);
    vec3 yw = mod(vec3(p0.y, p1.y, p2.y), per.y);
    vec3 iuw = xw + 0.5 * yw;
    vec3 ivw = yw;

    // Create gradients from indices
    vec2 g0 = rgrad2(vec2(iuw.x, ivw.x), rot);
    vec2 g1 = rgrad2(vec2(iuw.y, ivw.y), rot);
    vec2 g2 = rgrad2(vec2(iuw.z, ivw.z), rot);

    // Gradients dot vectors to corresponding corners
    // (The derivatives of this are simply the gradients)
    vec3 w = vec3(dot(g0, d0), dot(g1, d1), dot(g2, d2));

    // Radial weights from corners
    // 0.8 is the square of 2/sqrt(5), the distance from
    // a grid point to the nearest simplex boundary
    vec3 t = 0.8 - vec3(dot(d0, d0), dot(d1, d1), dot(d2, d2));

    // Set influence of each surflet to zero outside radius sqrt(0.8)
    t = max(t, 0.0);

    // Fourth power of t
    vec3 t2 = t * t;
    vec3 t4 = t2 * t2;

    // Final noise value is:
    // sum of ((radial weights) times (gradient dot vector from corner))
    float n = dot(t4, w);

    // Rescale to cover the range [-1,1] reasonably well
    return pNoiseRepeatVector.x*n;
}

//
// 2-D tiling simplex noise with fixed gradients,
// without the analytical derivative.
// This function is implemented as a wrapper to "psrnoise",
// at the minimal cost of three extra additions.
//
float psnoise(vec2 pos, vec2 per) {
    return psrnoise(pos, per, 0.0);
}

//
// 2-D non-tiling simplex noise with rotating gradients and analytical derivative.
// The first component of the 3-element return vector is the noise value,
// and the second and third components are the x and y partial derivatives.
//
vec3 srdnoise(vec2 pos, float rot) {
    // Offset y slightly to hide some rare artifacts
    pos.y += 0.001;
    // Skew to hexagonal grid
    vec2 uv = vec2(pos.x + pos.y*0.5, pos.y);

    vec2 i0 = floor(uv);
    vec2 f0 = fract(uv);
    // Traversal order
    vec2 i1 = (f0.x > f0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);

    // Unskewed grid points in (x,y) space
    vec2 p0 = vec2(i0.x - i0.y * 0.5, i0.y);
    vec2 p1 = vec2(p0.x + i1.x - i1.y * 0.5, p0.y + i1.y);
    vec2 p2 = vec2(p0.x + 0.5, p0.y + 1.0);

    // Integer grid point indices in (u,v) space
    i1 = i0 + i1;
    vec2 i2 = i0 + vec2(1.0, 1.0);

    // Vectors in unskewed (x,y) coordinates from
    // each of the simplex corners to the evaluation point
    vec2 d0 = pos - p0;
    vec2 d1 = pos - p1;
    vec2 d2 = pos - p2;

    vec3 x = vec3(p0.x, p1.x, p2.x);
    vec3 y = vec3(p0.y, p1.y, p2.y);
    vec3 iuw = x + 0.5 * y;
    vec3 ivw = y;

    // Avoid precision issues in permutation
    iuw = mod289(iuw);
    ivw = mod289(ivw);

    // Create gradients from indices
    vec2 g0 = rgrad2(vec2(iuw.x, ivw.x), rot);
    vec2 g1 = rgrad2(vec2(iuw.y, ivw.y), rot);
    vec2 g2 = rgrad2(vec2(iuw.z, ivw.z), rot);

    // Gradients dot vectors to corresponding corners
    // (The derivatives of this are simply the gradients)
    vec3 w = vec3(dot(g0, d0), dot(g1, d1), dot(g2, d2));

    // Radial weights from corners
    // 0.8 is the square of 2/sqrt(5), the distance from
    // a grid point to the nearest simplex boundary
    vec3 t = 0.8 - vec3(dot(d0, d0), dot(d1, d1), dot(d2, d2));

    // Partial derivatives for analytical gradient computation
    vec3 dtdx = -2.0 * vec3(d0.x, d1.x, d2.x);
    vec3 dtdy = -2.0 * vec3(d0.y, d1.y, d2.y);

    // Set influence of each surflet to zero outside radius sqrt(0.8)
    if (t.x < 0.0) {
        dtdx.x = 0.0;
        dtdy.x = 0.0;
        t.x = 0.0;
    }
    if (t.y < 0.0) {
        dtdx.y = 0.0;
        dtdy.y = 0.0;
        t.y = 0.0;
    }
    if (t.z < 0.0) {
        dtdx.z = 0.0;
        dtdy.z = 0.0;
        t.z = 0.0;
    }

    // Fourth power of t (and third power for derivative)
    vec3 t2 = t * t;
    vec3 t4 = t2 * t2;
    vec3 t3 = t2 * t;

    // Final noise value is:
    // sum of ((radial weights) times (gradient dot vector from corner))
    float n = dot(t4, w);

    // Final analytical derivative (gradient of a sum of scalar products)
    vec2 dt0 = vec2(dtdx.x, dtdy.x) * 4.0 * t3.x;
    vec2 dn0 = t4.x * g0 + dt0 * w.x;
    vec2 dt1 = vec2(dtdx.y, dtdy.y) * 4.0 * t3.y;
    vec2 dn1 = t4.y * g1 + dt1 * w.y;
    vec2 dt2 = vec2(dtdx.z, dtdy.z) * 4.0 * t3.z;
    vec2 dn2 = t4.z * g2 + dt2 * w.z;

    return pNoiseRepeatVector.x*vec3(n, dn0 + dn1 + dn2);
}

//
// 2-D non-tiling simplex noise with fixed gradients and analytical derivative.
// This function is implemented as a wrapper to "srdnoise",
// at the minimal cost of three extra additions.
//
vec3 sdnoise(vec2 pos) {
    return srdnoise(pos, 0.0);
}

//
// 2-D non-tiling simplex noise with rotating gradients,
// without the analytical derivative.
//
float srnoise(vec2 pos, float rot) {
    // Offset y slightly to hide some rare artifacts
    pos.y += 0.001;
    // Skew to hexagonal grid
    vec2 uv = vec2(pos.x + pos.y*0.5, pos.y);

    vec2 i0 = floor(uv);
    vec2 f0 = fract(uv);
    // Traversal order
    vec2 i1 = (f0.x > f0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);

    // Unskewed grid points in (x,y) space
    vec2 p0 = vec2(i0.x - i0.y * 0.5, i0.y);
    vec2 p1 = vec2(p0.x + i1.x - i1.y * 0.5, p0.y + i1.y);
    vec2 p2 = vec2(p0.x + 0.5, p0.y + 1.0);

    // Integer grid point indices in (u,v) space
    i1 = i0 + i1;
    vec2 i2 = i0 + vec2(1.0, 1.0);

    // Vectors in unskewed (x,y) coordinates from
    // each of the simplex corners to the evaluation point
    vec2 d0 = pos - p0;
    vec2 d1 = pos - p1;
    vec2 d2 = pos - p2;

    // Wrap i0, i1 and i2 to the desired period before gradient hashing:
    // wrap points in (x,y), map to (u,v)
    vec3 x = vec3(p0.x, p1.x, p2.x);
    vec3 y = vec3(p0.y, p1.y, p2.y);
    vec3 iuw = x + 0.5 * y;
    vec3 ivw = y;

    // Avoid precision issues in permutation
    iuw = mod289(iuw);
    ivw = mod289(ivw);

    // Create gradients from indices
    vec2 g0 = rgrad2(vec2(iuw.x, ivw.x), rot);
    vec2 g1 = rgrad2(vec2(iuw.y, ivw.y), rot);
    vec2 g2 = rgrad2(vec2(iuw.z, ivw.z), rot);

    // Gradients dot vectors to corresponding corners
    // (The derivatives of this are simply the gradients)
    vec3 w = vec3(dot(g0, d0), dot(g1, d1), dot(g2, d2));

    // Radial weights from corners
    // 0.8 is the square of 2/sqrt(5), the distance from
    // a grid point to the nearest simplex boundary
    vec3 t = 0.8 - vec3(dot(d0, d0), dot(d1, d1), dot(d2, d2));

    // Set influence of each surflet to zero outside radius sqrt(0.8)
    t = max(t, 0.0);

    // Fourth power of t
    vec3 t2 = t * t;
    vec3 t4 = t2 * t2;

    // Final noise value is:
    // sum of ((radial weights) times (gradient dot vector from corner))
    float n = dot(t4, w);

    // Rescale to cover the range [-1,1] reasonably well
    return pNoiseRepeatVector.x*n;
}

float normalnoise(vec2 p) {
    float noise = 0.0;
    if (noiseType == 0) {
        noise = perlin_2d(p);
    } else if (noiseType == 1) {
        noise = cnoise(p);
    } else if (noiseType == 2) {
        noise = pnoise(p, pNoiseRepeatVector.zw);
    } else if (noiseType == 3) {
        noise = snoise(p);
    } else if (noiseType == 4) {
        noise = srnoise(p, pNoiseRepeatVector.y);//power(3,5)
    } else if (noiseType == 5) {
        noise = srdnoise(p, pNoiseRepeatVector.y).x;//power(4,5.5)
    } else if (noiseType == 6) {
        noise = psrnoise(p, pNoiseRepeatVector.zw, pNoiseRepeatVector.y);//power(4,5.5), vec(0.75,3)
    } else if (noiseType == 7) {
        noise = psrdnoise(p, pNoiseRepeatVector.zw, pNoiseRepeatVector.y).x;//power(4,5.5), vec(0.75,3)
    }
    return noise * 0.5 + 0.5;
}

float noise(vec2 p) {
    p += offset;
    const int steps = 5;
    float scale = pow(2.0, float(steps));
    float displace = 0.0;
    for (int i = 0; i < steps; i++) {
        displace = normalnoise(p * scale + displace);
        scale *= 0.5;
    }
    return normalnoise(p + displace);
}

void main() {
    vec3 s = texture(textureOpaque, textureCoords0).rgb;
    float n = noise(gl_FragCoord.xy * scale * 1.0);
    n = pow(n + density, falloff);

    fragColor = vec4(mix(s, color, n), 1f);
}