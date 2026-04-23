package me.shurik.bettersuggestions.client.mixin;

import me.shurik.bettersuggestions.client.utils.text.TextCompletions;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Clear translation cache when the language manager reloads.
 */
@Mixin(LanguageManager.class)
public class LanguageManagerMixin {
    // public void onResourceManagerReload(ResourceManager manager)
    @Inject(at = @At("HEAD"), method = "onResourceManagerReload")
    void reload(ResourceManager resourceManager, CallbackInfo info) {
        TextCompletions.TRANSLATION_CACHE.clear();
    }
}
