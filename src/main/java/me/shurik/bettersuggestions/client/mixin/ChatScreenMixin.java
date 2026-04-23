package me.shurik.bettersuggestions.client.mixin;

import me.shurik.bettersuggestions.client.Client;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Clear any lingering entity highlight when the chat screen closes, since
 * {@code extractRenderState} will not fire again to clear it for us.
 */
@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(at = @At("HEAD"), method = "removed")
    void clearHighlightOnClose(CallbackInfo ci) {
        Client.clearHighlightedEntity();
    }
}
