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

public class OreVeinConfig {
    public final BlockStateProvider ore;
    public final BlockStateProvider secondary_ore;
    public final float secondary_ore_chance;
    public final BlockStateProvider fillerBlock;
    public final OreRichnessSettings veinSettings;
    public final List<ResourceKey<Level>> dimension;
    public final DensityFunctionThreshold veinToggle;
    public final DensityFunctionThreshold veinRidged;
    public final DensityFunctionThreshold veinGap;
    public final IVeinCondition conditions;

    public static final Codec<OreVeinConfig> CODEC = RecordCodecBuilder.create(oreVeinConfigInstance -> oreVeinConfigInstance.group(
        ResourceKeyOrBlockState.CODEC.fieldOf("ore").forGetter(config -> config.ore),
        ResourceKeyOrBlockState.CODEC.fieldOf("secondary_ore").forGetter(config -> config.secondary_ore),
        Codec.floatRange(0.0f, 1.0f).fieldOf("secondary_ore_chance").orElse(0.02f).forGetter(config -> config.secondary_ore_chance),
        ResourceKeyOrBlockState.CODEC.fieldOf("filler_block").forGetter(config -> config.fillerBlock),
        DensityFunctionThreshold.CODEC.optionalFieldOf("vein_toggle", new DensityFunctionThreshold(null, DensityFunctionThreshold.DEFAULT_MIN_THRESHOLD, DensityFunctionThreshold.DEFAULT_MAX_THRESHOLD)).forGetter(config -> config.veinToggle),
        DensityFunctionThreshold.CODEC.optionalFieldOf("vein_ridged", new DensityFunctionThreshold(null, DensityFunctionThreshold.DEFAULT_MIN_THRESHOLD, DensityFunctionThreshold.DEFAULT_MAX_THRESHOLD)).forGetter(config -> config.veinRidged),
        DensityFunctionThreshold.CODEC.optionalFieldOf("vein_gap", new DensityFunctionThreshold(null, DensityFunctionThreshold.DEFAULT_MIN_THRESHOLD, DensityFunctionThreshold.DEFAULT_MAX_THRESHOLD)).forGetter(config -> config.veinGap),
        OreRichnessSettings.CODEC.optionalFieldOf("vein_settings", OreRichnessSettings.createDefault()).forGetter(config -> config.veinSettings),
        IsDimension.CODEC.fieldOf("dimension").orElse(Collections.singletonList(Level.OVERWORLD)).forGetter(config -> config.dimension),
        VeinConditionRegistry.codec().optionalFieldOf("conditions", new AlwaysTrueCondition()).forGetter(config -> config.conditions)
    ).apply(oreVeinConfigInstance, OreVeinConfig::new));

    public OreVeinConfig(BlockStateProvider ore, BlockStateProvider secondaryOre, float secondaryOreChance, BlockStateProvider fillerBlock, DensityFunctionThreshold veinToggle, DensityFunctionThreshold veinRidged, DensityFunctionThreshold veinGap, OreRichnessSettings veinSettings, List<ResourceKey<Level>> dimension, IVeinCondition conditions){
        this.ore = ore;
        this.secondary_ore = secondaryOre;
        this.secondary_ore_chance = secondaryOreChance;
        this.fillerBlock = fillerBlock;
        this.veinToggle = veinToggle;
        this.veinRidged = veinRidged;
        this.veinGap = veinGap;
        this.veinSettings = veinSettings;
        this.dimension = dimension;
        this.conditions = conditions;
    }
}