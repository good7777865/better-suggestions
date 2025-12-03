package me.shurik.bettersuggestions.client.mixin;

import me.shurik.bettersuggestions.client.access.ClientEntityDataAccessor;
import me.shurik.bettersuggestions.client.access.EntityRenderStateAccessor;
import me.shurik.bettersuggestions.client.render.SpecialRendererQueue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.InteractionEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderManager.class)
public abstract class EntityRenderManagerMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void shouldRender(E entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
        // Only ignore display entity view range when highlighted (fixes #16)
        if (entity instanceof AreaEffectCloudEntity || (entity instanceof DisplayEntity && ((ClientEntityDataAccessor)entity).isHighlighted())) {
            info.setReturnValue(true);
        }
    }

    // Store entity reference in EntityRenderState since render() method only receives the state, not the entity
    @Inject(method = "getAndUpdateRenderState", at = @At("RETURN"))
    private <E extends Entity> void storeEntityInState(E entity, float tickDelta, CallbackInfoReturnable<EntityRenderState> cir) {
        EntityRenderState state = cir.getReturnValue();
        if (state instanceof EntityRenderStateAccessor accessor) {
            accessor.bettersuggestions$setSourceEntity(entity);
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private <S extends EntityRenderState> void renderHighlight(
            S renderState,
            CameraRenderState cameraRenderState,
            double d, double e, double f,
            MatrixStack matrices,
            OrderedRenderCommandQueue orderedRenderCommandQueue,
            CallbackInfo ci
    ) {
        // Retrieve the original entity from the render state
        if (!(renderState instanceof EntityRenderStateAccessor accessor)) {
            return;
        }

        Entity entity = accessor.bettersuggestions$getSourceEntity();

        if (entity != null && ((ClientEntityDataAccessor)entity).isHighlighted()) {
            switch (entity) {
                case MarkerEntity markerEntity -> suggestions$renderItem(Items.STRUCTURE_VOID.getDefaultStack(), matrices, orderedRenderCommandQueue, entity, d, e, f);
                case AreaEffectCloudEntity areaEffectCloudEntity -> suggestions$renderItem(Items.LINGERING_POTION.getDefaultStack(), matrices, orderedRenderCommandQueue, entity, d, e, f);
                case DisplayEntity displayEntity -> SpecialRendererQueue.addEntity(entity);
                case InteractionEntity interactionEntity -> SpecialRendererQueue.addEntity(entity);
                // In case the proper renderer is broken:
                // case InteractionEntity interaction -> suggestions$renderItem(Items.PISTON.getDefaultStack(), matrices, orderedRenderCommandQueue, entity, d, e, f);
                // case DisplayEntity display -> suggestions$renderItem(Items.ITEM_FRAME.getDefaultStack(), matrices, orderedRenderCommandQueue, entity, d, e, f);
                default -> {}
            }
        }
    }

    @Unique
    private void suggestions$renderItem(ItemStack item, MatrixStack matrices, OrderedRenderCommandQueue commandQueue, Entity entity, double x, double y, double z) {
        matrices.push();

        matrices.translate(x, y, z);

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        matrices.multiply(camera.getRotation());

        matrices.scale(0.8f, 0.8f, 0.8f);

        MinecraftClient client = MinecraftClient.getInstance();
        ItemModelManager modelManager = client.getItemModelManager();
        ItemRenderState itemRenderState = new ItemRenderState();
        modelManager.clearAndUpdate(itemRenderState, item, ItemDisplayContext.FIXED, client.world, entity, entity.getId());

        itemRenderState.render(matrices, commandQueue, 15728880, OverlayTexture.DEFAULT_UV, 0);

        matrices.pop();
    }
}