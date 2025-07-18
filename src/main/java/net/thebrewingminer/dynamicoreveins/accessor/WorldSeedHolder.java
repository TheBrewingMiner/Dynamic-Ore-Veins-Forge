package net.thebrewingminer.dynamicoreveins.accessor;

public class WorldSeedHolder {
    private static long worldSeed = 0L;

    public static void setSeed(long seed) {
        worldSeed = seed;
    }

    public static long getSeed() {
        return worldSeed;
    }
}
