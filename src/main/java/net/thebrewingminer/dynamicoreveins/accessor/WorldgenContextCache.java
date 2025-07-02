package net.thebrewingminer.dynamicoreveins.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldgenContextCache {
    private static final Map<ResourceKey<Level>, WGContext> dimensionContextMap = new ConcurrentHashMap<>();

    public static void setContext(ResourceKey<Level> dimension, ChunkGenerator generator, LevelHeightAccessor heightAccessor) {
        dimensionContextMap.put(dimension, new WGContext(generator, heightAccessor));
    }

    public static WGContext getContext(ResourceKey<Level> dimension) {
        return dimensionContextMap.get(dimension);
    }

    public static ChunkGenerator getGenerator(ResourceKey<Level> dimension) {
        WGContext wgContext = getContext(dimension);
        return wgContext != null ? wgContext.generator : null;
    }

    public static LevelHeightAccessor getHeightAccessor(ResourceKey<Level> dimension) {
        WGContext wgContext = getContext(dimension);
        return wgContext != null ? wgContext.heightAccessor : null;
    }

    public record WGContext(ChunkGenerator generator, LevelHeightAccessor heightAccessor){}
}