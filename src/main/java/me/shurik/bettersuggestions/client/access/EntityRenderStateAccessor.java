package me.shurik.bettersuggestions.client.access;

import net.minecraft.entity.Entity;

public interface EntityRenderStateAccessor {
    void bettersuggestions$setSourceEntity(Entity entity);
    Entity bettersuggestions$getSourceEntity();
}