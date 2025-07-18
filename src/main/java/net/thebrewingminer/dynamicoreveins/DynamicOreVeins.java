package net.thebrewingminer.dynamicoreveins;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(DynamicOreVeins.MOD_ID)
public class DynamicOreVeins {
    public static final String MOD_ID = "dynamic_ore_veins";

    public DynamicOreVeins(){
        MinecraftForge.EVENT_BUS.register(this);
    }
}