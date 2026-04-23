package me.shurik.bettersuggestions.client.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessors to overwrite Brigadier's {@code final} fields on {@link ArgumentCommandNode}.
 *
 * <p>We use this to rewrite the client-received command tree at {@code handleCommands} TAIL.
 * {@code customSuggestions} gets swapped when {@code ASK_SERVER} is stuck on a provider we want
 * to answer locally. {@code type} gets swapped when we want the client to parse/suggest a node
 * using a completely different {@link ArgumentType} than what the server sent — e.g. treating a
 * player-only {@code EntityArgument} or a {@code ScoreHolderArgument} as a non-player
 * {@code EntityArgument} client-side so entity-selector suggestions and UUID parsing both work
 * without any server cooperation.
 */
@Mixin(value = ArgumentCommandNode.class, remap = false)
public interface ArgumentCommandNodeAccessor {
    @Mutable
    @Accessor("customSuggestions")
    void bettersuggestions$setCustomSuggestions(SuggestionProvider<?> provider);

    @Mutable
    @Accessor("type")
    void bettersuggestions$setType(ArgumentType<?> type);
}
