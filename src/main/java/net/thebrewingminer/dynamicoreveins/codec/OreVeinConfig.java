package net.thebrewingminer.dynamicoreveins.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.thebrewingminer.dynamicoreveins.codec.condition.AlwaysTrueCondition;
import net.thebrewingminer.dynamicoreveins.codec.condition.DensityFunctionThreshold;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;
import net.thebrewingminer.dynamicoreveins.codec.condition.IsDimension;
import net.thebrewingminer.dynamicoreveins.registry.VeinConditionRegistry;

import java.util.Collections;
import java.util.List;

public record OreVeinConfig(BlockStateProvider ore, BlockStateProvider secondaryOre, float secondaryOreChance, BlockStateProvider fillerBlock, DensityFunctionThreshold veinToggle, DensityFunctionThreshold veinRidged, DensityFunctionThreshold veinGap, OreRichnessSettings veinSettings, List<ResourceKey<Level>> dimension, IVeinCondition conditions){
    public static final Codec<OreVeinConfig> CODEC = RecordCodecBuilder.create(oreVeinConfigInstance -> oreVeinConfigInstance.group(
        ResourceKeyOrBlockState.CODEC.fieldOf("ore").forGetter(OreVeinConfig::ore),
        ResourceKeyOrBlockState.CODEC.fieldOf("secondary_ore").forGetter(OreVeinConfig::secondaryOre),
        Codec.floatRange(0.0f, 1.0f).fieldOf("secondary_ore_chance").orElse(0.02f).forGetter(OreVeinConfig::secondaryOreChance),
        ResourceKeyOrBlockState.CODEC.fieldOf("filler_block").forGetter(config -> config.fillerBlock),
        DensityFunctionThreshold.CODEC.optionalFieldOf("vein_toggle", DensityFunctionThreshold.createDefault()).forGetter(OreVeinConfig::veinToggle),
        DensityFunctionThreshold.CODEC.optionalFieldOf("vein_ridged", DensityFunctionThreshold.createDefault()).forGetter(OreVeinConfig::veinRidged),
        DensityFunctionThreshold.CODEC.optionalFieldOf("vein_gap", DensityFunctionThreshold.createDefault()).forGetter(OreVeinConfig::veinGap),
        OreRichnessSettings.CODEC.optionalFieldOf("vein_settings", OreRichnessSettings.createDefault()).forGetter(OreVeinConfig::veinSettings),
        IsDimension.CODEC.fieldOf("dimension").orElse(Collections.singletonList(Level.OVERWORLD)).forGetter(OreVeinConfig::dimension),
        VeinConditionRegistry.codec().optionalFieldOf("conditions", new AlwaysTrueCondition()).forGetter(OreVeinConfig::conditions)
    ).apply(oreVeinConfigInstance, OreVeinConfig::new));
}