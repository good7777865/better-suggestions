package me.shurik.bettersuggestions.client.mixin;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import me.shurik.bettersuggestions.client.Client;
import me.shurik.bettersuggestions.client.access.CustomSuggestionAccessor;
import me.shurik.bettersuggestions.client.utils.ClientUtils;
import me.shurik.bettersuggestions.utils.RegistryUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.shurik.bettersuggestions.ModConstants.CONFIG;

/**
 * Render tooltip while holding shift
 * Render custom text and tooltip
 * Highlight entities from suggestions
 * Sort suggestions
 */
@Debug(export = true)
@Mixin(targets = "net.minecraft.client.gui.components.CommandSuggestions$SuggestionsList", priority = 1001)
//                                                1001 - fix incompatibility with Figura mod
public class SuggestionWindowMixin {
    @Shadow @Final CommandSuggestions this$0;
    @Unique
    private static final LiteralMessage PLACEHOLDER_MESSAGE = new LiteralMessage("PLACEHOLDER");

    @Shadow private int offset;

    @Shadow @Final private Rect2i rect;

    @Shadow @Final private List<Suggestion> suggestionList;

    @Shadow private int current;

    
    @Unique private Font suggestions$textRenderer;
    @Unique private boolean suggestions$renderShiftTooltip;

    @Unique private boolean isMouseCompletion;

    @Inject(at = @At("TAIL"), method = "<init>")
    void init(CommandSuggestions commandSuggestions, int x, int y, int width, List<Suggestion> suggestions, boolean narrateFirstSuggestion, CallbackInfo info) {
        ChatInputSuggestorAccessorMixin suggestorAccessor = (ChatInputSuggestorAccessorMixin) this.this$0;
        this.suggestions$textRenderer = suggestorAccessor.getFont();

        // TODO: add color customization for chat and cmd block input
        // suggestor.owner instanceof ChatScreen and suggestor.owner instanceof AbstractCommandBlockScreen

        // Try modifying the suggestions list
        try {
            // https://stackoverflow.com/questions/8364856/how-to-test-if-a-list-extends-object-is-an-unmodifablelist
            this.suggestionList.addAll(Collections.emptyList());
        } catch (UnsupportedOperationException e) {
            // Silently exit if the list is unmodifiable
            return;
        }

        ArrayList<Suggestion> prioritizedSuggestions = new ArrayList<>();
        ArrayList<Suggestion> otherSuggestions = new ArrayList<>();

        int inputLength = suggestorAccessor.getTextField().getValue().length();

        Entity crosshairTarget = ClientUtils.getCrosshairTargetEntity();
        String crosshairTargetUuid = crosshairTarget != null ? crosshairTarget.getStringUUID() : null;

        // Sort all entity UUIDs to be displayed first
        for (Suggestion suggestion : suggestions) {
            CustomSuggestionAccessor customSuggestion = (CustomSuggestionAccessor) suggestion;

            // Prioritize config entries
            // Only if there is some input text
            if (inputLength != suggestion.getRange().getStart() && CONFIG.prioritizedSuggestions.contains(suggestion.getText())) {
                prioritizedSuggestions.add(suggestion);
            }
            // then entity UUIDs
            // (except the crosshair target, it will always be put first)
            else if (customSuggestion.isEntitySuggestion()) {

                // If the crosshair target exists and is the same as the suggestion, put it as first
                if (crosshairTargetUuid != null && crosshairTargetUuid.equals(customSuggestion.better_suggestions$getOriginalText())) {
                    prioritizedSuggestions.add(0, suggestion);

                    // Suggest entity selector if enabled
                    if (CONFIG.entitySuggestions.suggestEntitySelector) {
                        // 26.1.2+ added @n selector, which is equivalent to @e[limit=1,sort=nearest].
                        // Strip the vanilla "minecraft:" namespace since it is implied when parsing;
                        // leave modded namespaces untouched.
                        String typeName = RegistryUtils.getName(BuiltInRegistries.ENTITY_TYPE, crosshairTarget.getType());
                        if (typeName.startsWith("minecraft:")) {
                            typeName = typeName.substring("minecraft:".length());
                        }
                        String selector = String.format("@n[type=%s]", typeName);
                        StringRange stringRange = new StringRange(suggestion.getRange().getStart(), suggestion.getRange().getStart() + selector.length());
                        prioritizedSuggestions.add(1, new Suggestion(stringRange, selector));
                    }
                } else {
                    prioritizedSuggestions.add(suggestion);
                }
            } else {
                otherSuggestions.add(suggestion);
            }
        }

        this.suggestionList.clear();
        this.suggestionList.addAll(prioritizedSuggestions);
        this.suggestionList.addAll(otherSuggestions);
        select(0);
    }

    @Nullable
    @Unique
    private CustomSuggestionAccessor customCurrentSuggestion;

    // HEAD
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    void renderPrepare(GuiGraphicsExtractor context, int mouseX, int mouseY, CallbackInfo info) {
        suggestions$renderShiftTooltip = true;
        customCurrentSuggestion = null;
        // Clear the previous frame's highlight; renderFinish will set a new one if applicable.
        Client.clearHighlightedEntity();
    }

    // Suggestion suggestion = this.suggestionList.get(renderIndex + this.offset);
    @ModifyVariable(at = @At(value = "STORE"), method = "extractRenderState", ordinal = 0)
    public Suggestion captureSuggestion(Suggestion suggestion) {
        customCurrentSuggestion = (CustomSuggestionAccessor) suggestion;
        return suggestion;
    }

    // context.text(font, suggestion.getText(), ...)
    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V", ordinal = 0))
    void drawFormattedTextWithShadow(GuiGraphicsExtractor context, Font font, String __, int x, int y, int color) {
        assert customCurrentSuggestion != null;
        // Draw formatted text instead
        context.text(font, customCurrentSuggestion.getFormattedText(), x, y, color);
    }

    //                                                                           \/
    // if (renderTooltip && (message = this.suggestionList.get(this.current).getTooltip()) != null) ...
    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/suggestion/Suggestion;getTooltip()Lcom/mojang/brigadier/Message;", ordinal = 0, remap = false))
    Message ifBlockTooltipManipulation(Suggestion suggestion) {
        // If there's custom tooltip, return placeholder to make sure the if block succeeds
        if (!((CustomSuggestionAccessor) suggestion).getMultilineTooltip().isEmpty()) {
            return PLACEHOLDER_MESSAGE; // Avoid creating a new LiteralMessage every time
        } else {
            // Otherwise, return the original tooltip
            return suggestion.getTooltip();
        }
    }

    // context.setTooltipForNextFrame(font, ComponentUtils.fromMessage(message), mouseX, mouseY);
    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;II)V"))
    void renderMouseTooltip(GuiGraphicsExtractor context, Font font, Component text, int x, int y) {
        // Render custom tooltip
        CustomSuggestionAccessor customSuggestion = (CustomSuggestionAccessor)this.suggestionList.get(this.current);
        List<Component> tooltip = customSuggestion.getMultilineTooltip();
        if (tooltip != null) {
            context.setComponentTooltipForNextFrame(font, tooltip, x, y);
        }
        suggestions$renderShiftTooltip = false;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    void renderFinish(GuiGraphicsExtractor context, int mouseX, int mouseY, CallbackInfo info) {
        CustomSuggestionAccessor customSuggestion = (CustomSuggestionAccessor)this.suggestionList.get(this.current);

//        if (customSuggestion.isBlockPosSuggestion()) {
//            SpecialRendererQueue.addBlock(customSuggestion.getBlockPos());
//        } else if (customSuggestion.isPositionSuggestion()) {
//            SpecialRendererQueue.addPosition(customSuggestion.getPosition());
//        }

        // Render shift tooltip

        if (suggestions$renderShiftTooltip && (ClientUtils.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || ClientUtils.isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT))) {
            List<Component> tooltip = customSuggestion.getMultilineTooltip();
            if (tooltip != null) {
                //                                                                                                             get suggestion index in for loop
                context.setComponentTooltipForNextFrame(suggestions$textRenderer, tooltip, this.rect.getX() - 5, this.rect.getY() + (12 * (this.current - this.offset)) - 10 * (tooltip.size() - 1) - 1);
            }
        }

        // Highlight entity from selected suggestion
        if (customSuggestion.isEntitySuggestion()) {
            Entity entity = customSuggestion.better_suggestions$getEntity();
            if (entity != null) {
                Client.setHighlightedEntity(entity);
            }
        }
    }

    //public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    @Inject(method = "keyPressed", at=@At("HEAD"), cancellable = true)
    void keyPressed(net.minecraft.client.input.KeyEvent event, CallbackInfoReturnable<Boolean> info) {
        int keyCode = event.key(); int modifiers = event.modifiers();
        if (keyCode == GLFW.GLFW_KEY_UP && modifiers == 2) {
            // Don't forget the minus sign | Wrap around                                      Don't overscroll
            this.cycle(-(this.current == 0 ? 1 : (this.current - CONFIG.maxSuggestionsShown < 0 ? this.current : CONFIG.maxSuggestionsShown)));
                        info.setReturnValue(true);
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN && modifiers == 2) {
            //                               Wrap around                                      Don't overscroll
            this.cycle(this.current == this.suggestionList.size() - 1 ? 1 : (this.current + CONFIG.maxSuggestionsShown >= this.suggestionList.size() ? this.suggestionList.size() - this.current - 1 : CONFIG.maxSuggestionsShown));
                        info.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/CommandSuggestions$SuggestionsList;select(I)V", ordinal = 0, shift = At.Shift.AFTER))
    private void markMouseClickCompletion(CallbackInfoReturnable<Boolean> cir) {
        this.isMouseCompletion = true;
    }

    @SuppressWarnings("unchecked")
    @Redirect(method = "useSuggestion", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0))
    private <E> E modifySuggestionIfNeeded(List<E> list, int index) {
        if (this.isMouseCompletion && CONFIG.addWhitespaceOnMouseCompletion && list.get(index) instanceof Suggestion suggestion) {
            return (E) new Suggestion(
                    suggestion.getRange(),
                suggestion.getText() + " "
            );
        }
        return list.get(index);
    }

    @Inject(method = "useSuggestion", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/suggestion/Suggestion;apply(Ljava/lang/String;)Ljava/lang/String;", ordinal = 0, shift = At.Shift.AFTER, remap = false))
    private void removeCompletingSuggestionFlag(CallbackInfo ci) {
        // keepSuggestions no longer accessible
        this.isMouseCompletion = false;
    }

    @Shadow
    public void select(int index) {}

    @Shadow
    public void cycle(int offset) {}
}
