package net.thebrewingminer.dynamicoreveins.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.thebrewingminer.dynamicoreveins.DynamicOreVeins;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;

public class OreVeinRegistry {
        public static final ResourceKey<Registry<OreVeinConfig>> ORE_VEIN_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(DynamicOreVeins.MOD_ID, "ore_vein"));
}