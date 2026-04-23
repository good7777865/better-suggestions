package me.shurik.bettersuggestions.client.access;

import net.minecraft.world.entity.Entity;

public interface EntityRenderStateAccessor {
    void bettersuggestions$setSourceEntity(Entity entity);
    Entity bettersuggestions$getSourceEntity();
}
