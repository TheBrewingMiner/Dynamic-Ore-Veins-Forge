package net.thebrewingminer.dynamicoreveins.helper;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.VeinSettingsConfig;
import net.thebrewingminer.dynamicoreveins.main.DefaultVanillaVein;
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistryHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public final class PrepareList {
    private static double cachedShuffleSourceSeed = Double.NEGATIVE_INFINITY;

    private PrepareList(){}

    public static List<OreVeinConfig> prepareList(DensityFunction.FunctionContext functionContext, VeinSettingsConfig config, long worldSeed) {
        Registry<OreVeinConfig> veinRegistry = OreVeinRegistryHolder.getVeinRegistry();
        DensityFunction shuffleSource = config.shuffleSource();

        OreVeinConfig IRON_VEIN = DefaultVanillaVein.ironVein();
        OreVeinConfig COPPER_VEIN = DefaultVanillaVein.copperVein();

        List<OreVeinConfig> veinRegistryList = new ArrayList<>(veinRegistry.stream().toList());
        List<OreVeinConfig> veinList = new ArrayList<>();

        double rawNoise = shuffleSource.compute(functionContext);
        double shuffleSourceSeed = Math.floor(rawNoise);
        long combinedSeed = (Double.doubleToLongBits(shuffleSourceSeed) ^ worldSeed);
        long shufflingSeed = SeedMath.mixSeed(combinedSeed);

        Random random = new Random(shufflingSeed);

        if (config.vanillaVeinsEnabled()) {
            veinList.add(IRON_VEIN);
            veinList.add(COPPER_VEIN);
            if (config.vanillaVeinsPrioritized()) {
                Collections.shuffle(veinRegistryList, random);
                veinList.addAll(veinRegistryList);
            } else {
                veinList.addAll(veinRegistryList);
                Collections.shuffle(veinList, random);
            }
        } else {
            Collections.shuffle(veinRegistryList, random);
            veinList.addAll(veinRegistryList);
        }

        List<ResourceLocation> currentList = veinList.stream()
                .map(vein -> {
                    ResourceLocation key = veinRegistry.getKey(vein);
                    return key != null ? key : new ResourceLocation("unregistered");
                })
                .toList();

        if (!(cachedShuffleSourceSeed == shuffleSourceSeed)) {
            System.out.println("===================================================");
            System.out.println("[DOV] SHUFFLE SEED: " + shufflingSeed);
            System.out.println("[DOV] Shuffle Source value: " + shuffleSourceSeed);
            System.out.println("[DOV] Final shuffled vein list: " + currentList.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")));
            System.out.println("[DOV] VanillaVeinsEnabled:  " + config.vanillaVeinsEnabled());
            System.out.println("[DOV] VanillaVeinsPriority: " + config.vanillaVeinsPrioritized());
            System.out.println("===================================================");

            cachedShuffleSourceSeed = shuffleSourceSeed;
        }

        return veinList;
    }
}