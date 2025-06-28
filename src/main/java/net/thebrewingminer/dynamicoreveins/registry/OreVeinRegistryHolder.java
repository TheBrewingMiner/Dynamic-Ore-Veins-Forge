package net.thebrewingminer.dynamicoreveins.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;

public class OreVeinRegistryHolder {
    private static RegistryAccess access;
    public static final ResourceKey<Registry<OreVeinConfig>> REGISTRY = OreVeinRegistry.ORE_VEIN_REGISTRY;

    public static void init(RegistryAccess registryAccess) {
        access = registryAccess;
        getRegistry();
    }

    public static Registry<OreVeinConfig> getRegistry() {
        if (access == null) {
            throw new IllegalStateException("RegistryAccess has not been initialized.");
        }
        return access.registryOrThrow(REGISTRY);
    }

    public static boolean isInitialized(){
        return access != null;
    }
}