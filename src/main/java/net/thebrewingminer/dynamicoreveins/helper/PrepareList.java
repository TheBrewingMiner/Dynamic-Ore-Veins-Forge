package net.thebrewingminer.dynamicoreveins.helper;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.VeinSettingsConfig;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;
import net.thebrewingminer.dynamicoreveins.main.DefaultVanillaVein;
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistryHolder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import java.util.stream.Collectors;

import static net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistryHolder.getActiveDebugSettings;

public final class PrepareList {
    private static final Map<Double, List<OreVeinConfig>> cachedLists = new ConcurrentHashMap<>();
    private static volatile double cachedShuffleSourceSeed = Double.NEGATIVE_INFINITY;     // Debug

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
    public static List<OreVeinConfig> prepareList(double shuffleSourceSeed, long worldSeed, boolean vanillaVeinsEnabled, boolean vanillaVeinsPrioritized) {
        Registry<OreVeinConfig> veinRegistry = OreVeinRegistryHolder.getVeinRegistry();             // Get the datapack registry.
        List<OreVeinConfig> veinRegistryList = new ArrayList<>(veinRegistry.stream().toList());     // Append all into a list.
        List<OreVeinConfig> veinList = new ArrayList<>();       // List to build into the final returned list.

        long combinedSeed = (Double.doubleToLongBits(shuffleSourceSeed) ^ worldSeed);      // Convert to long and mix with the world seed.
        long shufflingSeed = SeedMath.mixSeed(combinedSeed);                               // Mix it up to amplify the "randomness" of the value.

        // Vanilla veins expressed via ore vein configs.
        OreVeinConfig IRON_VEIN = DefaultVanillaVein.ironVein();
        OreVeinConfig COPPER_VEIN = DefaultVanillaVein.copperVein();

        // Random instance to shuffle list with, seeded by the transformed shuffle source value.
        Random random = new Random(shufflingSeed);

        if (vanillaVeinsEnabled) {
            veinList.add(IRON_VEIN);                            // Add vanilla veins to the list.
            veinList.add(COPPER_VEIN);
            if (vanillaVeinsPrioritized) {
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

        return veinList;
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "CommentedOutCode", "RedundantSuppression"})
    public static List<OreVeinConfig> getOrShuffleList(DensityFunction.FunctionContext functionContext, IVeinCondition.Context veinContext){
        VeinSettingsConfig settings = OreVeinRegistryHolder.getActiveConfig();      // Get the first (highest priority) config.

        DensityFunction shuffleSource = settings.getOrMapFunction(veinContext);     // Get the wired function.

        double rawNoise = shuffleSource.compute(functionContext);                   // Get raw value.
        double shuffleSourceSeed = Math.floor(rawNoise);                            // Floor it so that it only provides integers.

        // Lazily compute the shuffled list and cache the result for the "region."
        List<OreVeinConfig> veinList = cachedLists.computeIfAbsent(shuffleSourceSeed, shuffledList -> prepareList(shuffleSourceSeed, veinContext.seed(), settings.vanillaVeinsEnabled(), settings.vanillaVeinsPrioritized()));

        if(getActiveDebugSettings().printShuffledList()){
            if (!(cachedShuffleSourceSeed == shuffleSourceSeed)) {
                Registry<OreVeinConfig> veinRegistry = OreVeinRegistryHolder.getVeinRegistry();
                List<ResourceLocation> currentList = veinList.stream()
                    .map(vein -> {
                        ResourceLocation key = veinRegistry.getKey(vein);
                        return key != null ? key : new ResourceLocation("unregistered");
                    })
                    .toList();

                System.out.println("===================================================");
                System.out.println("[DOV] Shuffle Source value: " + shuffleSourceSeed);
                System.out.println("[DOV] World Seed: " + veinContext.seed());
                System.out.println("[DOV] Final shuffled vein list: " + currentList.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")));
                System.out.println("[DOV] VanillaVeinsEnabled:  " + settings.vanillaVeinsEnabled());
                System.out.println("[DOV] VanillaVeinsPriority: " + settings.vanillaVeinsPrioritized());
                System.out.println("===================================================");

                cachedShuffleSourceSeed = shuffleSourceSeed;
            }
        }

        return veinList;
    }

    public static void clearCache(){
        cachedLists.clear();
    }
}