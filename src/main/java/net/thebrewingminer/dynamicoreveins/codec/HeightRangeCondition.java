package net.thebrewingminer.dynamicoreveins.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public record HeightRangeCondition(VerticalAnchor minInclusive, VerticalAnchor maxInclusive) implements IVeinCondition {
    public static final Codec<HeightRangeCondition> CODEC = RecordCodecBuilder.create(heightRangeConditionInstance -> heightRangeConditionInstance.group(
            VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(HeightRangeCondition::minInclusive),
            VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(HeightRangeCondition::maxInclusive)
    ).apply(heightRangeConditionInstance, HeightRangeCondition::new));

    @Override
    public String type(){
        return "minecraft:height_range";
    }

    @Override
    public boolean test(IVeinCondition.Context context){
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor());
        int minY = minInclusive.resolveY(worldGenerationContext);
        int maxY = maxInclusive.resolveY(worldGenerationContext);
        int posY = context.pos().getY();
        return (posY >= minY && posY <= maxY);
    }

    @Override
    public Codec<? extends IVeinCondition> codec() {
        return CODEC;
    }
}