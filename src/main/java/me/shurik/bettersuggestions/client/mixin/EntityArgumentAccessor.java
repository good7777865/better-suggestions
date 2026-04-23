package me.shurik.bettersuggestions.client.mixin;

import net.minecraft.commands.arguments.EntityArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor to overwrite {@link EntityArgument}'s {@code private final playersOnly} flag.
 *
 * <p>Used by {@link ClientPacketListenerCommandsMixin}: after the server's command tree is
 * deserialized on the client, we walk it and flip {@code playersOnly} to {@code false} on every
 * {@link EntityArgument} node. Forcing the field value itself — rather than intercepting each
 * read with a method hook — means every code path (vanilla parse/suggest, Via-translated flows,
 * anything else that touches the field) uniformly sees {@code false}.
 */
@Mixin(EntityArgument.class)
public interface EntityArgumentAccessor {
    @Mutable
    @Accessor("playersOnly")
    void bettersuggestions$setPlayersOnly(boolean value);
}
