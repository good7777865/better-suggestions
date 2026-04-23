package me.shurik.bettersuggestions.client.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for CommandSuggestions
 */
@Mixin(CommandSuggestions.class)
public interface ChatInputSuggestorAccessorMixin {
    @Accessor("input")
    EditBox getTextField();

    @Accessor
    Font getFont();
}
