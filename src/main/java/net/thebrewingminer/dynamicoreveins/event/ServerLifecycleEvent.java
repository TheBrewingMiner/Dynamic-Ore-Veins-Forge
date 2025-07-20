package net.thebrewingminer.dynamicoreveins.event;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thebrewingminer.dynamicoreveins.DynamicOreVeins;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.VeinSettingsConfig;
import net.thebrewingminer.dynamicoreveins.codec.condition.DensityFunctionThreshold;
import net.thebrewingminer.dynamicoreveins.helper.ExtractHeightConditions;
import net.thebrewingminer.dynamicoreveins.helper.PrepareList;
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistryHolder;

@Mod.EventBusSubscriber(modid = DynamicOreVeins.MOD_ID)
public class ServerLifecycleEvent {
    @SubscribeEvent
    public static void onServerStarted(ServerAboutToStartEvent event) {
        // Initializes holder by storing the registry access object.
        // Registries are retrieved using this access.
        if (!OreVeinRegistryHolder.isInitialized()) {
            RegistryAccess registryAccess = event.getServer().registryAccess();
            OreVeinRegistryHolder.init(registryAccess);
            System.out.println("[DOV] Registry initialized on server start.");

            // Verifies how many configs are loaded in the registry.
            Registry<OreVeinConfig> veinRegistry = OreVeinRegistryHolder.getVeinRegistry();
            System.out.println("[DOV] Ore Vein Registry size: " + veinRegistry.size());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent serverStoppedEvent){
        // Clears all caches on world close.

        DensityFunctionThreshold.clearCache();
        OreVeinRegistryHolder.getConfigRegistry().forEach(VeinSettingsConfig::clearCache);
        System.out.println("[DOV] Cleared density function caches.");

        ExtractHeightConditions.clearCache();
        System.out.println("[DOV] Cleared HeightRangeCondition lists cache.");

        PrepareList.clearCache();
        System.out.println("[DOV] Cleared shuffled vein lists cache.");
    }
}