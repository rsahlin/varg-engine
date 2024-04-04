
vec4 getVec4Input(uint16_t index) {
    return (index & 0x08000) != 0 ? opcode.outputData[(index & 0x07fff)] : vec4inputs.inputData[index];
}

float getFloatInput(uint16_t index) {
    return (index & 0x08000) != 0 ? opcode.outputData[(index & 0x07fff)].x : floatinputs.inputData[index];
}

vec3 transformInput(vec3 position) {
    vec3 offset = getVec4Input(opcodes.code[opcode.index++]).xyz;
    vec4 rotate = getVec4Input(opcodes.code[opcode.index++]);
    vec3 scale = getVec4Input(opcodes.code[opcode.index++]).xyz;
    position = position + 2.*cross(rotate.xyz, cross(rotate.xyz, position) + rotate.w * position); 
    position = position * scale;
    position += offset;
    return position;
}


vec3 hash33(vec3 p3) {
    p3 = fract(p3 * vec3(0.1031f, 0.1030f, 0.0973f));
//    vec3 p3_yxz = vec3(p3.y, p3.x, p3.z);
    p3 += dot(p3, p3.yxz + 33.33);
//    p3 = p3 + , glms_vec3_broadcast(glms_vec3_dot(p3, glms_vec3_add(glms_vec3_swizzle(p3, SWIZZLE_YXZ), glms_vec3_broadcast(33.33f)))));
    return fract((p3.xxy + p3.yxx)*p3.zyx);
//    return fract(glms_vec3_mul(glms_vec3_add(glms_vec3_swizzle(p3, SWIZZLE_XXY), glms_vec3_swizzle(p3, SWIZZLE_YXX)), glms_vec3_swizzle(p3, SWIZZLE_ZYX)));
}

vec3 random_gradient(vec3 p) {
    // generate random positive vector
    vec3 random_vector = hash33(p);
    // ensure the vector contains negative values
    random_vector -= 0.5f;
    return normalize(random_vector);
}


float dot_grid_gradient(vec3 position, vec3 current_cell_origin, vec3 neighbor_offset) {
    vec3 cell_position = current_cell_origin + neighbor_offset;
    vec3 offset_in_neighbor_cell = position - cell_position;
    return dot(offset_in_neighbor_cell, random_gradient(cell_position));
}

// https : //en.wikipedia.org/wiki/Perlin_noise
float perlin_noise(vec3 x)
{
    // Evaluate a spatially connected noise by generating a random value
    // for each corner point of a cube and smoothstep interpolating between them with the
    // fract component of the input.
    vec3 fraction = fract(x);
    vec3 tile_origin = floor(x); 

    // dot product of distance to grid of the sample point and the random gradient
    float dot_grid_grad_a = dot_grid_gradient(x, tile_origin, vec3(0.0f, 0.0f, 0.0f));
    float dot_grid_grad_b = dot_grid_gradient(x, tile_origin, vec3(1.0f, 0.0f, 0.0f));
    float dot_grid_grad_c = dot_grid_gradient(x, tile_origin, vec3(0.0f, 1.0f, 0.0f));
    float dot_grid_grad_d = dot_grid_gradient(x, tile_origin, vec3(1.0f, 1.0f, 0.0f));

    float dot_grid_grad_e = dot_grid_gradient(x, tile_origin, vec3(0.0f, 0.0f, 1.0f));
    float dot_grid_grad_f = dot_grid_gradient(x, tile_origin, vec3(1.0f, 0.0f, 1.0f));
    float dot_grid_grad_g = dot_grid_gradient(x, tile_origin, vec3(0.0f, 1.0f, 1.0f));
    float dot_grid_grad_h = dot_grid_gradient(x, tile_origin, vec3(1.0f, 1.0f, 1.0f));

    // evaluate smoothstep
    // vec3s u = glms_vec3_smoothstep(glms_vec3_zero(), glms_vec3_one(), fraction); //glms_vec3_mul(glms_vec3_mul(fraction, fraction), glms_vec3_sub(glms_vec3_broadcast(3.0), glms_vec3_scale(fraction, 2.0f)));

    // evaluate smootherstep
    vec3 u = fraction;
//    u = glms_vec3_mul(glms_vec3_mul(u, glms_vec3_mul(u, u)), glms_vec3_adds(glms_vec3_mul(u, glms_vec3_subs(glms_vec3_scale(u, 6.0), 15.0)), 10.0));
    u = (u * u * u) * (u * (u * 6.0 - 15.0) + 10);
    // evaluate cosinterpolate
    // vec3s u = fraction;
    // u.x = (0.5 - cosf(u.x * M_PI) / 2.0);
    // u.y = (0.5 - cosf(u.y * M_PI) / 2.0);
    // u.z = (0.5 - cosf(u.z * M_PI) / 2.0);
    return mix(
        mix(
            mix(dot_grid_grad_a, dot_grid_grad_b, u.x),
            mix(dot_grid_grad_c, dot_grid_grad_d, u.x),
            u.y),
        mix(
            mix(dot_grid_grad_e, dot_grid_grad_f, u.x),
            mix(dot_grid_grad_g, dot_grid_grad_h, u.x),
            u.y),
       u.z);
}

// from https://github.com/MaxBittker/glsl-voronoi-noise/blob/master/3d.glsl
// samples from a random unit cube at point p
// like book of shaders implementation of random2 but 3 dimensional
vec3 random3(vec3 p) {
    vec3 rand1 = { 12.9898f, 37.719f, 78.233f };
    vec3 rand2 = { 37.719f, 78.233f, 12.9898f };
    vec3 rand3 = { 78.233f, 12.9898f, 37.719f };
    vec3 tmp = {
        dot(p, rand1),
        dot(p, rand2),
        dot(p, rand3)
    };
    return fract((sin(tmp) * 43758.5453f));
}


