package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public record HeightRangeCondition(VerticalAnchor minInclusive, VerticalAnchor maxInclusive) implements IVeinCondition {
    // Encodes vertical anchors into a height range.
    public static final Codec<HeightRangeCondition> CODEC = RecordCodecBuilder.create(heightRangeConditionInstance -> heightRangeConditionInstance.group(
            VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(HeightRangeCondition::minInclusive),
            VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(HeightRangeCondition::maxInclusive)
    ).apply(heightRangeConditionInstance, HeightRangeCondition::new));

    @Override
    public String type(){
        // String identifier.
        return "dynamic_veins:height_range";
    }

    @Override
    public boolean test(IVeinCondition.Context context){
        // Test if the context's position is within the height range.
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor());
        int minY = minInclusive.resolveY(worldGenerationContext);
        int maxY = maxInclusive.resolveY(worldGenerationContext);
        int posY = context.pos().getY();
        return (posY >= minY && posY <= maxY);
    }

    public static HeightRangeCondition createDefault(){
        // Default to height range [bottomY + 4, topY - 4].
        return new HeightRangeCondition(VerticalAnchor.aboveBottom(4), VerticalAnchor.belowTop(4));
    }
}