package net.thebrewingminer.dynamicoreveins.event;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.thebrewingminer.dynamicoreveins.DynamicOreVeins;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.VeinSettingsConfig;
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistries;

@Mod.EventBusSubscriber(modid = DynamicOreVeins.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRegistryEvents {
    @SubscribeEvent
    public static void onRegisterDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(OreVeinRegistries.ORE_VEIN_REGISTRY, OreVeinConfig.CODEC);
        event.dataPackRegistry(OreVeinRegistries.VEIN_SETTINGS_REGISTRY, VeinSettingsConfig.CODEC);
    }
}