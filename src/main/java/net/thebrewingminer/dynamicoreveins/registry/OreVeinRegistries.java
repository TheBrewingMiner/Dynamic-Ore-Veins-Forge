package net.thebrewingminer.dynamicoreveins.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.VeinSettingsConfig;

public class OreVeinRegistries {
        public static final ResourceKey<Registry<OreVeinConfig>> ORE_VEIN_REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation("worldgen", "ore_vein"));
        public static final ResourceKey<Registry<VeinSettingsConfig>> VEIN_SETTINGS_REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation("config", "vein_settings"));
}