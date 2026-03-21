#version 150

in vec4 vertexColor;
in vec3 worldPos;
in float vTime;

uniform vec4 ColorTint;

out vec4 fragColor;

float hash(vec3 p) {
    p = fract(p * vec3(443.897, 441.423, 437.195));
    p += dot(p, p.yxz + 19.19);
    return fract((p.x + p.y) * p.z);
}

float noise(vec3 x) {
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);

    return mix(
        mix(mix(hash(i + vec3(0,0,0)), hash(i + vec3(1,0,0)), f.x),
            mix(hash(i + vec3(0,1,0)), hash(i + vec3(1,1,0)), f.x), f.y),
        mix(mix(hash(i + vec3(0,0,1)), hash(i + vec3(1,0,1)), f.x),
            mix(hash(i + vec3(0,1,1)), hash(i + vec3(1,1,1)), f.x), f.y),
        f.z
    );
}

float fbm(vec3 p) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for(int i = 0; i < 6; i++) {
        value += amplitude * noise(p * frequency);
        frequency *= 2.0;
        amplitude *= 0.5;
    }

    return value;
}

vec2 voronoi(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);

    float minDist = 1.0;
    float secondMin = 1.0;

    for(int z = -1; z <= 1; z++) {
        for(int y = -1; y <= 1; y++) {
            for(int x = -1; x <= 1; x++) {
                vec3 neighbor = vec3(float(x), float(y), float(z));
                vec3 point = hash(i + neighbor) * vec3(1.0) + neighbor;
                vec3 diff = point - f;
                float dist = length(diff);

                if(dist < minDist) {
                    secondMin = minDist;
                    minDist = dist;
                } else if(dist < secondMin) {
                    secondMin = dist;
                }
            }
        }
    }

    return vec2(minDist, secondMin - minDist);
}

void main() {
    vec3 p = worldPos * 0.15;

    vec3 flow1 = vec3(0.0, 1.0, 0.2);
    vec3 flow2 = vec3(0.3, -0.5, 0.1);

    vec3 p1 = p + flow1 * vTime * 0.4;
    vec3 p2 = p + flow2 * vTime * 0.3;

    vec2 vor1 = voronoi(p1 * 2.0);
    vec2 vor2 = voronoi(p2 * 3.0);

    float cells = vor1.x * 0.6 + vor2.x * 0.4;
    float edges = vor1.y * vor2.y;

    float flow = fbm(p1 * 2.0);
    float turbulence = fbm(p2 * 4.0 + vec3(vTime * 0.5));

    float shimmer = sin(cells * 20.0 + vTime * 3.0 + flow * 10.0) * 0.5 + 0.5;
    shimmer = pow(shimmer, 3.0);

    float reflection = pow(1.0 - cells, 4.0) * (0.5 + turbulence * 0.5);
    reflection += pow(edges, 2.0) * 0.3;

    vec3 baseColor = vertexColor.rgb * ColorTint.rgb;

    float luminance = dot(baseColor, vec3(0.299, 0.587, 0.114));
    vec3 metallic = mix(vec3(luminance), baseColor, 0.7);

    float brightness = 0.4 + flow * 0.3 + cells * 0.3;
    metallic *= brightness;

    vec3 chromaticShift = vec3(
        shimmer * 1.2,
        shimmer * 0.9,
        shimmer * 1.1
    );
    metallic += chromaticShift * 0.4;

    float specular = pow(reflection, 2.0) * 1.5;
    vec3 specularColor = mix(vec3(1.0), baseColor * 1.5, 0.3);
    metallic += specularColor * specular;

    float edgeGlow = pow(edges, 3.0) * shimmer * 0.6;
    metallic += baseColor * edgeGlow * 2.0;

    float ambient = 0.2 + turbulence * 0.1;
    metallic += baseColor * ambient * 0.3;

    metallic = clamp(metallic, 0.0, 2.0);

    fragColor = vec4(metallic, vertexColor.a * ColorTint.a);
}