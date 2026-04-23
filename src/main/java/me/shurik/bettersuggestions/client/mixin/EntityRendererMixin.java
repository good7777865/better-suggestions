package me.shurik.bettersuggestions.client.mixin;

import me.shurik.bettersuggestions.client.access.ClientEntityDataAccessor;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Marker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Render markers and area effect clouds as items.
 */
@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {
    // public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z)
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    void shouldRender(T entity, Frustum culler, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> info) {
        if ((entity instanceof Marker || entity instanceof AreaEffectCloud) && ((ClientEntityDataAccessor) entity).isHighlighted()) {
            info.setReturnValue(true);
        }
    }
}
