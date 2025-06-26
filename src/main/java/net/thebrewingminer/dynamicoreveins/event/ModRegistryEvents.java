package net.thebrewingminer.dynamicoreveins.event;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.thebrewingminer.dynamicoreveins.DynamicOreVeins;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistry;

@Mod.EventBusSubscriber(modid = DynamicOreVeins.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRegistryEvents {
    @SubscribeEvent
    public static void onRegisterDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(OreVeinRegistry.ORE_VEIN_REGISTRY_KEY, OreVeinConfig.CODEC);
    }
}