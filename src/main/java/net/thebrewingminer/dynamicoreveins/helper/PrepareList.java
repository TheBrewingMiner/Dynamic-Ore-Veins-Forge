package net.thebrewingminer.dynamicoreveins.helper;

import net.minecraft.core.Registry;
//import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.VeinSettingsConfig;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;
import net.thebrewingminer.dynamicoreveins.main.DefaultVanillaVein;
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistryHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
//import java.util.stream.Collectors;

public final class PrepareList {
//    private static double cachedShuffleSourceSeed = Double.NEGATIVE_INFINITY;     // Debug

    private PrepareList(){}

    /*
    *   This method serves two purposes:
    *
    *       1. Enable/disable vanilla ore veins if preferred.
    *
    *       2. Tiebreak between two or more ore veins defined with similar or exact same conditions by simply shuffling the list.
    *          The DynamicOreVeinifier passes over the vein list and tests for the first succeeding vein config, and shuffling the
    *          list by "region" is the simplest way to balance potential matches with spatial dominance. The "shuffle source" density
    *          function in the settings config defines the regions by being discretized, mixed into the world seed, and finally being
    *          transformed. This provides the seed for the Random object passed into Collections.shuffle().
    */
    public static List<OreVeinConfig> prepareList(DensityFunction.FunctionContext functionContext, IVeinCondition.Context veinContext) {
        Registry<OreVeinConfig> veinRegistry = OreVeinRegistryHolder.getVeinRegistry();     // Get the datapack registry.
        VeinSettingsConfig settings = OreVeinRegistryHolder.getActiveConfig();              // Get the first (highest priority) config.

        DensityFunction shuffleSource = settings.getOrMapFunction(veinContext);             // Get the wired function.

        // Vanilla veins expressed via ore vein configs.
        OreVeinConfig IRON_VEIN = DefaultVanillaVein.ironVein();
        OreVeinConfig COPPER_VEIN = DefaultVanillaVein.copperVein();

        List<OreVeinConfig> veinRegistryList = new ArrayList<>(veinRegistry.stream().toList());
        List<OreVeinConfig> veinList = new ArrayList<>();

        double rawNoise = shuffleSource.compute(functionContext);                                   // Get raw value.
        double shuffleSourceSeed = Math.floor(rawNoise);                                            // Floor it so that it only provides integers.
        long combinedSeed = (Double.doubleToLongBits(shuffleSourceSeed) ^ veinContext.seed());      // Convert to double and mix with the world seed.
        long shufflingSeed = SeedMath.mixSeed(combinedSeed);                                        // Mix it up to amplify the "randomness" of the value.

        Random random = new Random(shufflingSeed);

        if (settings.vanillaVeinsEnabled()) {
            veinList.add(IRON_VEIN);                            // Add vanilla veins to the list.
            veinList.add(COPPER_VEIN);
            if (settings.vanillaVeinsPrioritized()) {
                Collections.shuffle(veinRegistryList, random);  // Only shuffle the custom veins and append the result after the vanilla veins,
                veinList.addAll(veinRegistryList);              // therefor prioritizing the vanilla veins when considered by DynamicOreVeinifier.
            } else {
                veinList.addAll(veinRegistryList);              // Append the custom veins to the list, then shuffle the entire list,
                Collections.shuffle(veinList, random);          // meaning the vanilla veins are not prioritized.
            }
        } else {
            Collections.shuffle(veinRegistryList, random);      // Vanilla veins disabled. Don't add the vanilla veins, add the custom ones,
            veinList.addAll(veinRegistryList);                  // shuffle and add to the returned list.
        }

        // Debug
//        List<ResourceLocation> currentList = veinList.stream()
//                .map(vein -> {
//                    ResourceLocation key = veinRegistry.getKey(vein);
//                    return key != null ? key : new ResourceLocation("unregistered");
//                })
//                .toList();
//
//        if (!(cachedShuffleSourceSeed == shuffleSourceSeed)) {
//            System.out.println("===================================================");
//            System.out.println("[DOV] SHUFFLE SEED: " + shufflingSeed);
//            System.out.println("[DOV] Shuffle Source value: " + shuffleSourceSeed);
//            System.out.println("[DOV] World Seed: " + veinContext.seed());
//            System.out.println("[DOV] Final shuffled vein list: " + currentList.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")));
//            System.out.println("[DOV] VanillaVeinsEnabled:  " + settings.vanillaVeinsEnabled());
//            System.out.println("[DOV] VanillaVeinsPriority: " + settings.vanillaVeinsPrioritized());
//            System.out.println("===================================================");
//
//            cachedShuffleSourceSeed = shuffleSourceSeed;
//        }

        return veinList;
    }
}