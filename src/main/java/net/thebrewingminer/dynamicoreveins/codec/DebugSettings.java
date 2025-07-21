package net.thebrewingminer.dynamicoreveins.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DebugSettings(boolean printHeightRange, boolean printShuffledList, boolean printSuccessPos) {
    public static Codec<DebugSettings> CODEC = RecordCodecBuilder.create(debugSettingsInstance -> debugSettingsInstance.group(
       Codec.BOOL.fieldOf("height_range").orElse(false).forGetter(DebugSettings::printHeightRange),
       Codec.BOOL.fieldOf("shuffled_list").orElse(false).forGetter(DebugSettings::printShuffledList),
       Codec.BOOL.fieldOf("vein_success").orElse(false).forGetter(DebugSettings::printSuccessPos)
    ).apply(debugSettingsInstance, DebugSettings::new));

    public static DebugSettings createDefault(){
        return new DebugSettings(false, false, false);
    }
}