package net.thebrewingminer.dynamicoreveins.event;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thebrewingminer.dynamicoreveins.DynamicOreVeins;
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistryHolder;

@Mod.EventBusSubscriber(modid = DynamicOreVeins.MOD_ID)
public class RegistryInitEvent {
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            RegistryAccess registryAccess = serverLevel.registryAccess();

            if (!OreVeinRegistryHolder.isInitialized()) {
                OreVeinRegistryHolder.init(registryAccess);
            }
        }
    }

}
