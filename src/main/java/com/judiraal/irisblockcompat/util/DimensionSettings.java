package com.judiraal.irisblockcompat.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DimensionSettings(boolean enabled, String shaderPackName) {
    public static final Codec<DimensionSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enabled").forGetter(DimensionSettings::enabled),
            Codec.STRING.fieldOf("shader").forGetter(DimensionSettings::shaderPackName)
    ).apply(instance, DimensionSettings::new));
}
