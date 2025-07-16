package net.thebrewingminer.dynamicoreveins.event;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thebrewingminer.dynamicoreveins.DynamicOreVeins;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistryHolder;

@Mod.EventBusSubscriber(modid = DynamicOreVeins.MOD_ID)
public class RegistryInitEvent {
    @SubscribeEvent
    public static void onServerStarted(ServerAboutToStartEvent event) {
        if (!OreVeinRegistryHolder.isInitialized()) {
            RegistryAccess registryAccess = event.getServer().registryAccess();
            OreVeinRegistryHolder.init(registryAccess);
            System.out.println("[OreVeinRegistryHolder] Registry initialized on server start.");

            Registry<OreVeinConfig> veinRegistry = OreVeinRegistryHolder.getVeinRegistry();
            System.out.println("[OreVeinRegistryHolder] Ore Vein Registry size: " + veinRegistry.size());
        }
    }
}