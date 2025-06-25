package net.thebrewingminer.dynamicoreveins.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class OreVeinConfig {
    public final BlockStateProvider ore;
    public final BlockStateProvider raw_ore;
    public final BlockStateProvider fillerBlock;
    public final ResourceKey<Level> dimension;

    public static final Codec<ResourceKey<Level>> DIMENSION_CODEC = ResourceKey.codec(Registry.DIMENSION_REGISTRY);
    
    public static final Codec<OreVeinConfig> CODEC = RecordCodecBuilder.create(oreVeinConfigInstance -> oreVeinConfigInstance.group(
            ResourceKeyOrBlockState.CODEC.fieldOf("ore").forGetter(config -> config.ore),
            ResourceKeyOrBlockState.CODEC.fieldOf("raw_ore").forGetter(config -> config.raw_ore),
            ResourceKeyOrBlockState.CODEC.fieldOf("filler_block").forGetter(config -> config.fillerBlock),
            DIMENSION_CODEC.fieldOf("dimension").forGetter(config -> config.dimension)
    ).apply(oreVeinConfigInstance, OreVeinConfig::new));

    public OreVeinConfig(BlockStateProvider ore, BlockStateProvider rawOre, BlockStateProvider fillerBlock, ResourceKey<Level> dimension) {
        this.ore = ore;
        this.raw_ore = rawOre;
        this.fillerBlock = fillerBlock;
        this.dimension = dimension;
    }
}