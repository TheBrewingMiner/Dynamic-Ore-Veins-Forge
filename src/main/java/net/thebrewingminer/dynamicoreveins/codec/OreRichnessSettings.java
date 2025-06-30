package net.thebrewingminer.dynamicoreveins.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record OreRichnessSettings(float minRichness, float maxRichness, float minRichnessThreshold, float maxRichnessThreshold, float veinSolidness, float skipOreThreshold, int edgeRoundOffBegin, double maxEdgeRoundOff){
    public static final float DEFAULT_MIN_RICHNESS = 0.1f;
    public static final float DEFAULT_MAX_RICHNESS = 0.3f;
    public static final float DEFAULT_MIN_RICHNESS_THRESHOLD = 0.4f;
    public static final float DEFAULT_MAX_RICHNESS_THRESHOLD = 0.6f;
    public static final float DEFAULT_VEIN_SOLIDNESS = 0.7f;
    public static final float DEFAULT_SKIP_ORE_THRESHOLD = -0.3f;
    public static final int DEFAULT_EDGE_ROUNDOFF_BEGIN = 20;
    public static final double DEFAULT_MAX_EDGE_ROUNDOFF = 0.2;

    public static final Codec<OreRichnessSettings> CODEC = RecordCodecBuilder.create(oreRichnessSettingsInstance -> oreRichnessSettingsInstance.group(
            Codec.FLOAT.fieldOf("min_ore_richness").orElse(DEFAULT_MIN_RICHNESS).forGetter(OreRichnessSettings::minRichness),
            Codec.FLOAT.fieldOf("max_ore_richness").orElse(DEFAULT_MAX_RICHNESS).forGetter(OreRichnessSettings::maxRichness),
            Codec.FLOAT.fieldOf("min_richness_threshold").orElse(DEFAULT_MIN_RICHNESS_THRESHOLD).forGetter(OreRichnessSettings::minRichnessThreshold),
            Codec.FLOAT.fieldOf("max_richness_threshold").orElse(DEFAULT_MAX_RICHNESS_THRESHOLD).forGetter(OreRichnessSettings::maxRichnessThreshold),
            Codec.FLOAT.fieldOf("vein_solidness").orElse(DEFAULT_VEIN_SOLIDNESS).forGetter(OreRichnessSettings::veinSolidness),
            Codec.FLOAT.fieldOf("skip_ore_threshold").orElse(DEFAULT_SKIP_ORE_THRESHOLD).forGetter(OreRichnessSettings::skipOreThreshold),
            Codec.INT.fieldOf("edge_roundoff_begin").orElse(DEFAULT_EDGE_ROUNDOFF_BEGIN).forGetter(OreRichnessSettings::edgeRoundOffBegin),
            Codec.DOUBLE.fieldOf("max_edge_roundoff").orElse(DEFAULT_MAX_EDGE_ROUNDOFF).forGetter(OreRichnessSettings::maxEdgeRoundOff)
    ).apply(oreRichnessSettingsInstance, OreRichnessSettings::new));

    public static OreRichnessSettings createDefault(){
        return new OreRichnessSettings(
                DEFAULT_MIN_RICHNESS,
                DEFAULT_MAX_RICHNESS,
                DEFAULT_MIN_RICHNESS_THRESHOLD,
                DEFAULT_MAX_RICHNESS_THRESHOLD,
                DEFAULT_VEIN_SOLIDNESS,
                DEFAULT_SKIP_ORE_THRESHOLD,
                DEFAULT_EDGE_ROUNDOFF_BEGIN,
                DEFAULT_MAX_EDGE_ROUNDOFF
        );
    }
}
