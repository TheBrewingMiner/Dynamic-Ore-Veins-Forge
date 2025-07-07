package net.thebrewingminer.dynamicoreveins.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VeinSettingsConfig(boolean vanillaVeinsEnabled, boolean vanillaVeinsPrioritized){
    public static final Codec<VeinSettingsConfig> CODEC = RecordCodecBuilder.create(configInstance -> configInstance.group(
            Codec.BOOL.fieldOf("vanilla_veins_enabled").orElse(true).forGetter(VeinSettingsConfig::vanillaVeinsEnabled),
            Codec.BOOL.fieldOf("vanilla_priority").orElse(true).forGetter(VeinSettingsConfig::vanillaVeinsPrioritized)
    ).apply(configInstance, VeinSettingsConfig::new));
}