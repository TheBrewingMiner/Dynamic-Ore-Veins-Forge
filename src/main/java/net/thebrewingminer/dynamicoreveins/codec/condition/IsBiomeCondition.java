package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;

import static net.minecraft.core.QuartPos.fromBlock;

// Adapted from Lithostitched by Apollo404 (ApolloUnknownDev)
// Original source: https://github.com/Apollounknowndev/lithostitched
// Licensed under the MIT License
public record IsBiomeCondition(HolderSet<Biome> biomes) implements IVeinCondition {
    public static final Codec<IsBiomeCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(IsBiomeCondition::biomes)
    ).apply(instance, IsBiomeCondition::new));

    @Override
    public boolean test(Context context) {
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        Climate.Sampler sampler = context.randomState().sampler();
        int x = context.pos().getX();
        int y = context.pos().getY();
        int z = context.pos().getZ();

        Holder<Biome> currBiome = chunkGenerator.getBiomeSource().getNoiseBiome(fromBlock(x), fromBlock(y), fromBlock(z), sampler);
        return (this.biomes.contains(currBiome));
    }

    @Override
    public String type() {
        return "dynamic_veins:biome";
    }
}