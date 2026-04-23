package me.shurik.bettersuggestions.client.mixin;

import me.shurik.bettersuggestions.client.access.EntityRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.ref.WeakReference;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements EntityRenderStateAccessor {

    // WeakReference to prevent memory leak
    @Unique
    private WeakReference<Entity> bettersuggestions$sourceEntity;

    @Override
    public void bettersuggestions$setSourceEntity(Entity entity) {
        // Only store reference for highlighted entities to minimize overhead
        this.bettersuggestions$sourceEntity = entity != null ? new WeakReference<>(entity) : null;
    }

    @Override
    public Entity bettersuggestions$getSourceEntity() {
        return this.bettersuggestions$sourceEntity != null ? this.bettersuggestions$sourceEntity.get() : null;
    }
}
