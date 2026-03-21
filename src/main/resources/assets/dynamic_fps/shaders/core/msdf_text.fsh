#version 330

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float median(vec3 value) {
    return max(min(value.r, value.g), min(max(value.r, value.g), value.b));
}

void main() {
    vec4 sampled = texture(Sampler0, texCoord0);

    float alpha = sampled.a;
    bool hasMsdfChannels = abs(sampled.r - sampled.g) > 0.001 || abs(sampled.g - sampled.b) > 0.001;

    if (hasMsdfChannels) {
        float sdf = median(sampled.rgb);
        float edge = max(fwidth(sdf), 1.0 / 64.0);
        alpha *= clamp((sdf - 0.5) / edge + 0.5, 0.0, 1.0);
    } else {
        float edge = max(fwidth(alpha), 1.0 / 256.0);
        alpha = smoothstep(0.5 - edge, 0.5 + edge, alpha);
    }

    vec4 color = vec4(vertexColor.rgb, vertexColor.a * alpha);
    if (color.a < 0.01) {
        discard;
    }

    fragColor = color;
}
