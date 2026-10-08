#version 150

in vec2 texCoord;

uniform float GameTime;
uniform vec2 Resolution;

out vec4 fragColor;

void main() {
    vec2 uv = texCoord / Resolution;
    
    float t = GameTime * 0.5;
    
    vec3 dark1 = vec3(0.05, 0.05, 0.08);
    vec3 dark2 = vec3(0.12, 0.08, 0.20);
    
    float mixVal = 0.5 + 0.5 * sin(uv.x * 3.0 + t + uv.y * 2.0);
    vec3 color = mix(dark1, dark2, mixVal);
    
    float vignette = 1.0 - length(uv - 0.5) * 0.8;
    color *= vignette;
    
    fragColor = vec4(color, 1.0);
}