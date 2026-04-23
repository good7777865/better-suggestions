package me.shurik.bettersuggestions.suggestion;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.CommandContext;
import me.shurik.bettersuggestions.event.ServerEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.server.ServerFunctionManager;

import java.util.List;

import static me.shurik.bettersuggestions.ModConstants.CONFIG;

public class FunctionArgumentTypeSuggestions {
    public static boolean filteredFunctionListInitialized = false;
    public static final List<Identifier> filteredFunctionList = Lists.newArrayList();

    public static void init() {
        FunctionCommand.SUGGEST_FUNCTION = (context, builder) -> {
            ServerFunctionManager functionManager = context.getSource().getServer().getFunctions();
            SharedSuggestionProvider.suggestResource(functionManager.getTagNames(), builder, "#");
            if (CONFIG.functionSuggestions.hideUnderscoreFunctions) {
                if (!filteredFunctionListInitialized) {
                    initFilteredFunctionList(context);
                }
                return SharedSuggestionProvider.suggestResource(filteredFunctionList, builder);
            } else {
                return SharedSuggestionProvider.suggestResource(functionManager.getFunctionNames(), builder);
            }
        };

        // Mark the filtered function list to initialize when reloading datapacks
        ServerEvents.START_DATA_PACK_RELOAD.register((server) -> {
            filteredFunctionListInitialized = false;
        });
    }

    private static void initFilteredFunctionList(CommandContext<CommandSourceStack> context) {
        context.getSource().getServer().getFunctions().getFunctionNames().forEach((s) -> {
            // Check if the function name starts with "_"
            if (!s.getPath().substring(s.getPath().lastIndexOf("/") + 1).startsWith("_")) {
                filteredFunctionList.add(s);
            }
        });
        filteredFunctionListInitialized = true;
    }
}
