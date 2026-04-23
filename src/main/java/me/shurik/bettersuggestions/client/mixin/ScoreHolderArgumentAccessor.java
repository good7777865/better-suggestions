package me.shurik.bettersuggestions.client.mixin;

import net.minecraft.commands.arguments.ScoreHolderArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Read-only accessor for {@link ScoreHolderArgument#multiple}. We need to know whether a given
 * node expects one target or many so we can pick the right replacement — {@code EntityArgument.entity()}
 * vs. {@code EntityArgument.entities()} — when swapping its type out on the client.
 */
@Mixin(ScoreHolderArgument.class)
public interface ScoreHolderArgumentAccessor {
    @Accessor("multiple")
    boolean bettersuggestions$getMultiple();
}
