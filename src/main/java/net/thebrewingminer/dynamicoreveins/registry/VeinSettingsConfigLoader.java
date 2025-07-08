package net.thebrewingminer.dynamicoreveins.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.thebrewingminer.dynamicoreveins.codec.VeinSettingsConfig;

import java.util.Map;

public class VeinSettingsConfigLoader extends SimpleJsonResourceReloadListener {
    public static final String PATH = "config/vein_settings";
    public static final Gson GSON = new GsonBuilder().create();
    public static VeinSettingsConfig ACTIVE_CONFIG = VeinSettingsConfig.createDefault();

    public VeinSettingsConfigLoader(){ super(GSON, PATH); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet()) {
            JsonElement json = entry.getValue();

            var result = VeinSettingsConfig.CODEC.parse(JsonOps.INSTANCE, json);
            if (result.result().isPresent()) {
                ACTIVE_CONFIG = result.result().get();
                return;
            }
        }
    }

    public static VeinSettingsConfig get() {
        return ACTIVE_CONFIG;
    }
}
