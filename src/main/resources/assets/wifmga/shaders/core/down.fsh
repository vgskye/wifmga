#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 baseOffset;

out vec4 fragColor;

void main() {
    vec4 sum = texture(DiffuseSampler, texCoord) * 4.0;
    sum += texture(DiffuseSampler, texCoord - baseOffset.xy);
    sum += texture(DiffuseSampler, texCoord + baseOffset.xy);
    sum += texture(DiffuseSampler, texCoord + vec2(baseOffset.x, -baseOffset.y));
    sum += texture(DiffuseSampler, texCoord - vec2(baseOffset.x, -baseOffset.y));

    fragColor = sum / 8.0;
}