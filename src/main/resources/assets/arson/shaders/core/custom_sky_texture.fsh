#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 sky = texture(Sampler0, texCoord);
    fragColor = vec4(sky.rgb * ColorModulator.rgb, sky.a * ColorModulator.a);
}
