#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 baseOffset;

out vec4 fragColor;

void main() {
    vec4 sum = texture(DiffuseSampler, texCoord + vec2(-baseOffset.x * 2.0, 0.0));
    sum += texture(DiffuseSampler, texCoord + vec2(-baseOffset.x, baseOffset.y)) * 2.0;
    sum += texture(DiffuseSampler, texCoord + vec2(0.0, baseOffset.y * 2.0));
    sum += texture(DiffuseSampler, texCoord + vec2(baseOffset.x, baseOffset.y)) * 2.0;
    sum += texture(DiffuseSampler, texCoord + vec2(baseOffset.x * 2.0, 0.0));
    sum += texture(DiffuseSampler, texCoord + vec2(baseOffset.x, -baseOffset.y)) * 2.0;
    sum += texture(DiffuseSampler, texCoord + vec2(0.0, -baseOffset.y * 2.0));
    sum += texture(DiffuseSampler, texCoord + vec2(-baseOffset.x, -baseOffset.y)) * 2.0;

    fragColor = sum / 12.0;
}