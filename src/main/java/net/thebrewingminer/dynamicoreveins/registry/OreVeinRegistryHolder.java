package net.thebrewingminer.dynamicoreveins.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.VeinSettingsConfig;

import java.util.Map;

public class OreVeinRegistryHolder {
    private static RegistryAccess access;
    public static final ResourceKey<Registry<OreVeinConfig>> VEIN_REGISTRY = OreVeinRegistries.ORE_VEIN_REGISTRY;
    public static final ResourceKey<Registry<VeinSettingsConfig>> CONFIG_REGISTRY = OreVeinRegistries.VEIN_SETTINGS_REGISTRY;

    public static void init(RegistryAccess registryAccess) {
        // Stores a registry access instance.
        access = registryAccess;
    }

    public static Registry<OreVeinConfig> getVeinRegistry() {
        // Grabs the vein registry with registry-awareness.
        if (access == null) {
            throw new IllegalStateException("RegistryAccess has not been initialized.");
        }
        return access.registryOrThrow(VEIN_REGISTRY);
    }

    public static Registry<VeinSettingsConfig> getConfigRegistry() {
        // Grabs the config registry with registry-awareness.
        if (access == null) {
            throw new IllegalStateException("RegistryAccess has not been initialized.");
        }
        return access.registryOrThrow(CONFIG_REGISTRY);
    }

    public static VeinSettingsConfig getActiveConfig() {
        // Grabs the highest priority config.
        return getConfigRegistry().entrySet().stream()
            .findFirst()
            .map(Map.Entry::getValue)
            .orElseThrow(() -> new IllegalStateException("No VeinSettingsConfig present in registry!"));
    }

    public static boolean isInitialized(){
        return access != null;
    }
}