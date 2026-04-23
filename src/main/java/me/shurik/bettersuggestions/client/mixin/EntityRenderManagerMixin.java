package me.shurik.bettersuggestions.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shurik.bettersuggestions.client.access.ClientEntityDataAccessor;
import me.shurik.bettersuggestions.client.access.EntityRenderStateAccessor;
import me.shurik.bettersuggestions.client.render.SpecialRendererQueue;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderManagerMixin {

    @Unique
    private static final float ITEM_RENDER_SCALE = 0.8f;

    // LightTexture.FULL_BRIGHT equivalent (sky=15<<20 | block=15<<4) — not exposed as a constant in 26.1.2.
    @Unique
    private static final int FULL_BRIGHT = 0xF000F0;

    // Lazy-initialized stacks. Can't eagerly assign in <clinit>: this mixin is merged into
    // EntityRenderDispatcher, whose static init runs before item components are bound, so
    // Items.X.getDefaultInstance() would NPE inside Holder$Reference#components.
    @Unique
    private static ItemStack MARKER_ITEM;
    @Unique
    private static ItemStack AEC_ITEM;

    @Unique
    private static ItemStack suggestions$markerItem() {
        ItemStack s = MARKER_ITEM;
        if (s == null) s = MARKER_ITEM = Items.STRUCTURE_VOID.getDefaultInstance();
        return s;
    }

    @Unique
    private static ItemStack suggestions$aecItem() {
        ItemStack s = AEC_ITEM;
        if (s == null) s = AEC_ITEM = Items.LINGERING_POTION.getDefaultInstance();
        return s;
    }

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void shouldRender(E entity, Frustum culler, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> info) {
        // Fast path: skip the instanceof chain for the overwhelming majority of entities
        // that are not highlighted by this mod.
        if (!((ClientEntityDataAccessor) entity).isHighlighted()) {
            return;
        }
        if (entity instanceof AreaEffectCloud || entity instanceof Marker || entity instanceof Display) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "extractEntity", at = @At("RETURN"))
    private <E extends Entity> void storeEntityInState(E entity, float partialTicks, CallbackInfoReturnable<EntityRenderState> cir) {
        if (!(entity instanceof ClientEntityDataAccessor clientAccessor) || !clientAccessor.isHighlighted()) {
            return;
        }

        // Display uses the vanilla glow pipeline (MinecraftClientMixin#shouldEntityAppearGlowing),
        // so we don't need to stash the source entity for it — saves the field write + later dispatch.
        if (entity instanceof Display) {
            return;
        }

        EntityRenderState state = cir.getReturnValue();
        if (state instanceof EntityRenderStateAccessor accessor) {
            accessor.bettersuggestions$setSourceEntity(entity);
        }
    }

    @Inject(method = "submit", at = @At("HEAD"))
    private <S extends EntityRenderState> void renderHighlight(
            S renderState,
            CameraRenderState camera,
            double x, double y, double z,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CallbackInfo ci
    ) {
        if (!(renderState instanceof EntityRenderStateAccessor accessor)) {
            return;
        }

        Entity entity = accessor.bettersuggestions$getSourceEntity();
        if (entity == null) {
            return;
        }

        switch (entity) {
            case Marker ignored -> suggestions$renderAsItem(
                    suggestions$markerItem(), poseStack, submitNodeCollector, entity, x, y, z);
            case AreaEffectCloud ignored -> suggestions$renderAsItem(
                    suggestions$aecItem(), poseStack, submitNodeCollector, entity, x, y, z);
            case Interaction ignored -> SpecialRendererQueue.addEntity(entity);
            // Display (item/block/text) relies on the vanilla glow pipeline (MinecraftClientMixin#shouldEntityAppearGlowing)
            default -> {}
        }
    }

    @Unique
    private void suggestions$renderAsItem(ItemStack item, PoseStack matrices, SubmitNodeCollector submitNodeCollector,
                                          Entity entity, double x, double y, double z) {
        Minecraft client = Minecraft.getInstance();
        ItemModelResolver resolver = client.getItemModelResolver();

        ItemStackRenderState stackRenderState = new ItemStackRenderState();
        resolver.updateForNonLiving(stackRenderState, item, ItemDisplayContext.GROUND, entity);

        if (stackRenderState.isEmpty()) {
            return;
        }

        matrices.pushPose();
        // x/y/z are already camera-relative (passed to submit from EntityRenderDispatcher).
        matrices.translate(x, y, z);

        Camera camera = client.gameRenderer.getMainCamera();
        matrices.mulPose(camera.rotation());
        matrices.scale(ITEM_RENDER_SCALE, ITEM_RENDER_SCALE, ITEM_RENDER_SCALE);

        // Always render at full brightness regardless of ambient light.
        stackRenderState.submit(matrices, submitNodeCollector, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);

        matrices.popPose();
    }
}
