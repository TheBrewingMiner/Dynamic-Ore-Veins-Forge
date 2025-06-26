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

import java.util.List;

public class OreVeinConfig{
    public final BlockStateProvider ore;
    public final BlockStateProvider secondary_ore;
    public final float secondary_ore_chance;
    public final BlockStateProvider fillerBlock;
    public final List<ResourceKey<Level>> dimension;
    public final DensityFunctionThreshold veinToggle;
    public final IVeinCondition conditions;

    public static final Codec<OreVeinConfig> CODEC = RecordCodecBuilder.create(oreVeinConfigInstance -> oreVeinConfigInstance.group(
            ResourceKeyOrBlockState.CODEC.fieldOf("ore").forGetter(config -> config.ore),
            ResourceKeyOrBlockState.CODEC.fieldOf("secondary_ore").forGetter(config -> config.secondary_ore),
            Codec.floatRange(0.0f, 1.0f).fieldOf("secondary_ore_chance").forGetter(config -> config.secondary_ore_chance),
            ResourceKeyOrBlockState.CODEC.fieldOf("filler_block").forGetter(config -> config.fillerBlock),
            IsDimension.CODEC.fieldOf("dimension").forGetter(config -> config.dimension),
            DensityFunctionThreshold.CODEC.fieldOf("vein_toggle").orElse(null).forGetter(config -> config.veinToggle),
            VeinConditionRegistry.CODEC.optionalFieldOf("conditions", new AlwaysTrueCondition()).forGetter(config -> config.conditions)
    ).apply(oreVeinConfigInstance, OreVeinConfig::new));

    public OreVeinConfig(BlockStateProvider ore, BlockStateProvider secondaryOre, float secondaryOreChance, BlockStateProvider fillerBlock, List<ResourceKey<Level>> dimension, DensityFunctionThreshold veinToggle, IVeinCondition conditions){
        this.ore = ore;
        this.secondary_ore = secondaryOre;
        this.secondary_ore_chance = secondaryOreChance;
        this.fillerBlock = fillerBlock;
        this.dimension = dimension;
        this.veinToggle = veinToggle;
        this.conditions = conditions;
    }
}