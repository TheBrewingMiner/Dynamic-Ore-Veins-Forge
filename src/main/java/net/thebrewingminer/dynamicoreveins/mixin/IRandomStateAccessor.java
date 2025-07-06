package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RandomState.class)
public interface IRandomStateAccessor {
    @Accessor("random")
    PositionalRandomFactory randomFactory();
}
