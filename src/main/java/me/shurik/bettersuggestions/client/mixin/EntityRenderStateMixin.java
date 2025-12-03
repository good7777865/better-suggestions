package me.shurik.bettersuggestions.client.mixin;

import me.shurik.bettersuggestions.client.access.EntityRenderStateAccessor;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements EntityRenderStateAccessor {

    @Unique
    private Entity bettersuggestions$sourceEntity;

    @Override
    public void bettersuggestions$setSourceEntity(Entity entity) {
        this.bettersuggestions$sourceEntity = entity;
    }

    @Override
    public Entity bettersuggestions$getSourceEntity() {
        return this.bettersuggestions$sourceEntity;
    }
}