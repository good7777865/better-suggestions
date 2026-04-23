package me.shurik.bettersuggestions.client.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.suggestion.Suggestion;
import me.shurik.bettersuggestions.client.access.CustomSuggestionAccessor;
import me.shurik.bettersuggestions.client.render.SpecialRendererQueue;
import me.shurik.bettersuggestions.utils.ColorUtils;
import me.shurik.bettersuggestions.utils.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static me.shurik.bettersuggestions.ModConstants.CONFIG;

/**
 *  Make maxSuggestionSize configurable
 *  Make suggestion window use formatted text for width calculation
 */
@Mixin(CommandSuggestions.class)
public class ChatInputSuggestorMixin {
    private static final List<SpecialRendererQueue.BlockEntry> blockRenderQueue = new ArrayList<>();
    private static final List<SpecialRendererQueue.PositionEntry> positionRenderQueue = new ArrayList<>();
    static {
        // Add this list to the block highlight renderer queue
        SpecialRendererQueue.BLOCKS.addList("chatInputSuggestor", blockRenderQueue);
        SpecialRendererQueue.POSITIONS.addList("chatInputSuggestor", positionRenderQueue);
    }
    private static final Vector4f[] COLORS = new Vector4f[] {
        ColorUtils.getColor(ChatFormatting.AQUA.getColor(), 0.3f),
        ColorUtils.getColor(ChatFormatting.YELLOW.getColor(), 0.3f),
        ColorUtils.getColor(ChatFormatting.GREEN.getColor(), 0.3f),
        ColorUtils.getColor(ChatFormatting.LIGHT_PURPLE.getColor(), 0.3f),
        ColorUtils.getColor(ChatFormatting.GOLD.getColor(), 0.3f)
    };
    private static Vector4f getColorForIndex(int index) { return COLORS[index % COLORS.length]; }

    @Mutable @Shadow @Final
    private int suggestionLineLimit;

    @Shadow private ParseResults<SharedSuggestionProvider> currentParse;

    @Redirect(method = "showSuggestions", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/suggestion/Suggestion;getText()Ljava/lang/String;", remap = false))
    String getAsFormattedText(Suggestion suggestion) { return ((CustomSuggestionAccessor)suggestion).getFormattedText().getString(); }

    @Inject(method = "showSuggestions", at = @At("HEAD"))
    void setMaxSuggestionSize(boolean immediateNarration, CallbackInfo info) { suggestionLineLimit = CONFIG.maxSuggestionsShown; }

    @Inject(method = "updateCommandInfo", at = @At("TAIL"))
    void grabCoordinates(CallbackInfo ci) {
        if (!CONFIG.highlightCoordinates) {
            return;
        }
        // Clear own block highlights
        blockRenderQueue.clear();
        positionRenderQueue.clear();
        if (currentParse == null) {
            return;
        }

        for (ParsedArgument<SharedSuggestionProvider, ?> parsedArgument : currentParse.getContext().getLastChild().getArguments().values()) {
            if (parsedArgument.getResult() instanceof WorldCoordinates) {
                // TODO: Ask server to get worldCoordinates.toAbsolutePos() instead of parsing it on the client
                String posString = parsedArgument.getRange().get(currentParse.getReader());
                if (StringUtils.isBlockPos(posString)) {
                    BlockPos pos = StringUtils.parseBlockPos(parsedArgument.getRange().get(currentParse.getReader()));
                    blockRenderQueue.add(new SpecialRendererQueue.BlockEntry(pos, getColorForIndex(blockRenderQueue.size())));
                } else if (StringUtils.isPosition(posString)) {
                    Vec3 pos = StringUtils.parsePosition(posString);
                    positionRenderQueue.add(new SpecialRendererQueue.PositionEntry(pos, getColorForIndex(blockRenderQueue.size())));
                }
            }
        }
    }
}
