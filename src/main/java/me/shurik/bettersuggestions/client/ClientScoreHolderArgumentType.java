package me.shurik.bettersuggestions.client;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.EntityArgument;

import java.util.concurrent.CompletableFuture;

/**
 * Client-side drop-in replacement for {@link net.minecraft.commands.arguments.ScoreHolderArgument}.
 *
 * <p>Provides entity / UUID suggestions (via an {@link EntityArgument} delegate) while tolerating
 * arbitrary score-holder strings — including fake-player names such as {@code #var} or
 * {@code myVariable} — that {@link EntityArgument} would otherwise reject with a syntax error.
 *
 * <ul>
 *   <li><b>Suggestions</b> — fully delegated to EntityArgument, so nearby entity UUIDs and
 *       {@code @e} / {@code @n} selectors appear in the suggestion list.</li>
 *   <li><b>Parse</b> — tries the entity delegate first; on failure, resets the cursor and
 *       skips the token by consuming characters until the next space, then returns {@code null}.
 *       The parsed value is never used on the client (execution goes to the server verbatim),
 *       so {@code null} is safe and avoids surfacing a red syntax-error highlight.</li>
 * </ul>
 */
public class ClientScoreHolderArgumentType implements ArgumentType<Object> {
    private final EntityArgument delegate;

    private ClientScoreHolderArgumentType(EntityArgument delegate) {
        this.delegate = delegate;
    }

    /** Replacement for {@code ScoreHolderArgument.scoreHolder()} (single target). */
    public static ClientScoreHolderArgumentType single() {
        return new ClientScoreHolderArgumentType(EntityArgument.entity());
    }

    /** Replacement for {@code ScoreHolderArgument.scoreHolders()} (multiple targets). */
    public static ClientScoreHolderArgumentType multiple() {
        return new ClientScoreHolderArgumentType(EntityArgument.entities());
    }

    @Override
    public Object parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        try {
            return delegate.parse(reader);
        } catch (CommandSyntaxException ignored) {
            // Fake-player names (e.g. #var, myVariable) are valid score holders on the server
            // but not valid entity selectors.  Reset and skip over the token so the client
            // parser advances correctly without showing a red error.
            reader.setCursor(cursor);
            while (reader.canRead() && reader.peek() != ' ') {
                reader.skip();
            }
            return null; // Never used client-side; server re-parses the raw text.
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return delegate.listSuggestions(context, builder);
    }
}
