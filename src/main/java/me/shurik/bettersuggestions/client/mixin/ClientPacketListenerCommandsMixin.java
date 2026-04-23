package me.shurik.bettersuggestions.client.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Rewrite the command tree the server just sent us so the client parses/suggests as if every
 * entity-like argument accepted any entity. Runs at {@code TAIL} of {@code handleCommands} —
 * by then {@code this.commands} holds the freshly-built dispatcher.
 *
 * <h2>Why a blanket type swap (not field tweaks or suggestion providers)</h2>
 * Earlier attempts tweaked vanilla behavior piecemeal — flip {@code playersOnly} to {@code false}
 * via {@code @ModifyExpressionValue}, or replace {@code ASK_SERVER} with a local
 * {@code SuggestionProvider} that mirrors the vanilla score-holder logic. In practice neither
 * survived every environment: ViaVersion bridging routes around the expression hook on parse
 * paths, and on unmodded servers the custom-suggestion replacement still didn't surface entity
 * UUIDs in {@code /tag} / {@code /scoreboard} / {@code /team}. The reliable fix is the obvious
 * one the user has been asking for: <strong>replace the argument type outright</strong>.
 *
 * <p>For every {@link ArgumentCommandNode} whose type is either a player-only
 * {@link EntityArgument} or a {@link ScoreHolderArgument}, we swap {@link ArgumentCommandNode#getType()}
 * to {@code EntityArgument.entity()} / {@code EntityArgument.entities()} (matching singular vs.
 * plural) and clear {@code customSuggestions} to {@code null}, so Brigadier falls through to
 * {@code EntityArgument.listSuggestions} which is both:
 * <ul>
 *   <li>fully client-side (no server round-trip needed),</li>
 *   <li>non-players-only (so nearby entity UUIDs and {@code @e}/{@code @n} selectors show up).</li>
 * </ul>
 *
 * <p>Server-side validity: we only change how the <em>client</em> parses/suggests. The typed
 * text goes to the server as-is and the server parses it against its unmodified tree. For
 * score-holder commands ({@code /scoreboard}, {@code /team}, {@code /tag}) the server's
 * {@code ScoreHolderArgument.parse} happily accepts raw UUIDs and selectors, so execution works.
 * For {@code EntityArgument.players()} commands (e.g. {@code /give}) the server still enforces
 * players-only at execute time — the user knows this limitation and accepts it (UI stops
 * blocking, server has the final word).
 *
 * <p>The BFS walks root + all descendants.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerCommandsMixin {
    @Shadow
    private CommandDispatcher<ClientSuggestionProvider> commands;

    @Inject(method = "handleCommands", at = @At("TAIL"))
    private void bettersuggestions$rewriteCommandTree(ClientboundCommandsPacket packet, CallbackInfo ci) {
        CommandDispatcher<ClientSuggestionProvider> dispatcher = this.commands;
        if (dispatcher == null) return;

        Deque<CommandNode<ClientSuggestionProvider>> queue = new ArrayDeque<>();
        queue.add(dispatcher.getRoot());
        while (!queue.isEmpty()) {
            CommandNode<ClientSuggestionProvider> node = queue.poll();
            if (node instanceof ArgumentCommandNode<?, ?> arg) {
                ArgumentType<?> type = arg.getType();
                ArgumentType<?> replacement = bettersuggestions$entityReplacementFor(type);
                if (replacement != null) {
                    ArgumentCommandNodeAccessor access = (ArgumentCommandNodeAccessor) arg;
                    access.bettersuggestions$setType(replacement);
                    // Clear ASK_SERVER (or any other provider) so Brigadier uses the new type's
                    // own listSuggestions — which is what we actually want to surface.
                    access.bettersuggestions$setCustomSuggestions(null);
                }
            }
            queue.addAll(node.getChildren());
        }
    }

    private static ArgumentType<?> bettersuggestions$entityReplacementFor(ArgumentType<?> type) {
        if (type instanceof ScoreHolderArgument sha) {
            return ((ScoreHolderArgumentAccessor) sha).bettersuggestions$getMultiple()
                ? EntityArgument.entities()
                : EntityArgument.entity();
        }
        // EntityArgument is either already entity-mode (nothing to do) or player-only (swap to
        // the entity-mode equivalent of the same arity).
        if (type instanceof EntityArgument) {
            // No public `playersOnly`/`single` getters, but the @Mutable accessor lets us just
            // overwrite the field in-place — simpler and cheaper than allocating a fresh arg.
            // Flip via EntityArgumentAccessor instead of type-swap here because we'd need a
            // `single` accessor too to pick the right factory, and mutating is equivalent.
            ((EntityArgumentAccessor) type).bettersuggestions$setPlayersOnly(false);
        }
        return null;
    }
}
