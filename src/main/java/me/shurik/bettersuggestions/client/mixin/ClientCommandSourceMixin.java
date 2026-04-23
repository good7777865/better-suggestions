package me.shurik.bettersuggestions.client.mixin;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

import static me.shurik.bettersuggestions.ModConstants.CONFIG;
import static me.shurik.bettersuggestions.client.Client.INSTANCE;

/**
 * Suggest nearby entities in selectors.
 */
@Mixin(ClientSuggestionProvider.class)
public class ClientCommandSourceMixin {
    @Inject(at = @At("HEAD"), method = "getSelectedEntities", cancellable = true)
    private void suggestNearbyEntities(CallbackInfoReturnable<Collection<String>> info) {
        if (INSTANCE.level != null && INSTANCE.player != null) {
            info.setReturnValue(INSTANCE.level.getEntitiesOfClass(Entity.class, INSTANCE.player.getBoundingBox().inflate(CONFIG.entitySuggestions.entitySuggestionRadius), (entity) -> !(entity instanceof Player)).stream().map(Entity::getScoreboardName).toList());
        }
    }
}
