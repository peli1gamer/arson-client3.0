#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in float skyHeight;
out vec4 fragColor;

void main() {
    vec3 horizon = vec3(0.30, 0.55, 0.92);
    vec3 zenith = vec3(0.025, 0.055, 0.16);
    float blend = smoothstep(0.0, 1.0, skyHeight);
    vec3 gradient = mix(horizon, zenith, blend);
    fragColor = vec4(gradient * ColorModulator.rgb, ColorModulator.a);
}
