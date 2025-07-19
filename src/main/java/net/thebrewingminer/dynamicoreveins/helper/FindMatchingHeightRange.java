package net.thebrewingminer.dynamicoreveins.helper;

import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.condition.HeightRangeCondition;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;

import java.util.List;

import static net.thebrewingminer.dynamicoreveins.helper.ExtractHeightConditions.extractHeightConditions;

public class FindMatchingHeightRange {
    // Helps take all height range conditions of the selected config and builds a min and max Y for vein computation given the
    // context's y position.
    private FindMatchingHeightRange(){}

    @SuppressWarnings("ReplaceNullCheck")
    public static HeightRangeWrapper findMatchingHeightRange(OreVeinConfig selectedConfig, IVeinCondition.Context veinContext) {
        List<HeightRangeCondition> conditionsList = extractHeightConditions(selectedConfig.conditions(), veinContext);   // Prepare a height range list.

        WorldGenerationContext worldGenContext = new WorldGenerationContext(veinContext.chunkGenerator(), veinContext.heightAccessor());
        HeightRangeWrapper firstMatchingRange = null;
        int matchingRangeCounter = 0;
        int DEFAULT_MIN_Y = worldGenContext.getMinGenY() + 4;
        int DEFAULT_MAX_Y = (DEFAULT_MIN_Y + worldGenContext.getGenDepth()) - 4;
        int minOverlapY = Integer.MIN_VALUE;
        int maxOverlapY = Integer.MAX_VALUE;
        int y = veinContext.pos().getY();

        for (HeightRangeCondition heightRange : conditionsList) {
            int minY = heightRange.minInclusive().resolveY(worldGenContext);
            int maxY = heightRange.maxInclusive().resolveY(worldGenContext);

            if (y >= minY && y <= maxY) {
                matchingRangeCounter++;
                if (firstMatchingRange == null){
                    firstMatchingRange = new HeightRangeWrapper(minY, maxY);    // For the first range the current pos is, find the min and max of that range.
                }
                if (minY > minOverlapY) minOverlapY = minY;                     // Set those as the found mins and maxes. If another height range is found for
                if (maxY < maxOverlapY) maxOverlapY = maxY;                     // this position, expand to find the min and max of all found ranges.
            }
        }

        if ((matchingRangeCounter > 1) && (minOverlapY <= maxOverlapY)){
            return (new HeightRangeWrapper(minOverlapY, maxOverlapY));      // Return a new range for the expanded range if more than one valid range was found for this pos.
        } else if (firstMatchingRange != null){                             // If only one valid range was found, return that one's range.
            return firstMatchingRange;
        } else {
            return new HeightRangeWrapper(DEFAULT_MIN_Y, DEFAULT_MAX_Y);    // Otherwise, default if no valid range was found.
        }
    }
}