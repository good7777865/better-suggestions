package me.shurik.bettersuggestions.suggestion;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;

import java.util.Collection;

/**
 * Replace the default ScoreHolderArgumentType suggestions with the one from entity selector.
 */
public class ScoreHolderArgumentTypeSuggestions {
    public static void init() {
        // ScoreHolderArgument.SUGGEST_SCORE_HOLDERS is now final in 26.1.2
        // TODO: use Mixin to override listSuggestions instead
        if (true) return;
        //noinspection UnreachableCode
        var unusedDummy = (com.mojang.brigadier.suggestion.SuggestionProvider<net.minecraft.commands.CommandSourceStack>) (context, builder) -> {
            Object source = context.getSource();
            if (source instanceof SharedSuggestionProvider commandSource) {
                StringReader reader = new StringReader(builder.getInput());
                reader.setCursor(builder.getStart());
                EntitySelectorParser entitySelectorParser = new EntitySelectorParser(reader, EntitySelectorParser.allowSelectors(commandSource));
                try {
                    entitySelectorParser.parse();
                } catch (CommandSyntaxException e) {
                    // Invalid entity selector
                }

                return entitySelectorParser.fillSuggestions(builder, (builderx) -> {
                    // I tried :(
                    // try {
                    //     // Suggest score holder names
                    //     // for the given objective
                    //     ScoreboardObjective objective = ScoreboardObjectiveArgumentType.getObjective(context, "objective");
                    //     Collection<ScoreboardPlayerScore> collection = Scoreboards.getScores(objective);
                    //     CommandSource.suggestMatching(collection.stream().map(ScoreboardPlayerScore::getPlayerName), builderx);
                    // } catch (CommandSyntaxException e) {
                    //     // No objective specified
                    // }
                    Collection<String> collection = commandSource.getOnlinePlayerNames();
                    Iterable<String> iterable = com.google.common.collect.Iterables.concat(collection, commandSource.getSelectedEntities());
                    SharedSuggestionProvider.suggest(iterable, builderx);
                });
            } else {
                return Suggestions.empty();
            }
        };
    }
}
