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
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistryHolder;

@Mod.EventBusSubscriber(modid = DynamicOreVeins.MOD_ID)
public class ServerLifecycleEvent {
    @SubscribeEvent
    public static void onServerStarted(ServerAboutToStartEvent event) {
        if (!OreVeinRegistryHolder.isInitialized()) {
            RegistryAccess registryAccess = event.getServer().registryAccess();
            OreVeinRegistryHolder.init(registryAccess);
            System.out.println("[DOV] Registry initialized on server start.");

            Registry<OreVeinConfig> veinRegistry = OreVeinRegistryHolder.getVeinRegistry();
            System.out.println("[DOV] Ore Vein Registry size: " + veinRegistry.size());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent serverStoppedEvent){
        DensityFunctionThreshold.clearCache();
        OreVeinRegistryHolder.getConfigRegistry().forEach(VeinSettingsConfig::clearCache);
        System.out.println("[DOV] Cleared density function caches.");
    }
}