package net.thebrewingminer.dynamicoreveins.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;

public class OreVeinRegistry {
        public static final ResourceKey<Registry<OreVeinConfig>> ORE_VEIN_REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation("worldgen", "ore_vein"));
}