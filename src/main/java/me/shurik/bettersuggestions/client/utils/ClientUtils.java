package me.shurik.bettersuggestions.client.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static me.shurik.bettersuggestions.client.Client.INSTANCE;

@Environment(EnvType.CLIENT)
public class ClientUtils {
    @Nullable
    public static Entity getEntityByUUID(ClientLevel world, UUID uuid) {
        if (world == null) return null;
        for (Entity entity : world.entitiesForRendering()) {
            if (entity.getUUID().equals(uuid)) {
                return entity;
            }
        }
        return null;
    }

    @Nullable
    public static Entity getEntityByUUID(String uuid) {
        return getEntityByUUID(INSTANCE.level, UUID.fromString(uuid));
    }

    public static boolean entityExists(ClientLevel world, int id) {
        return world != null && world.getEntity(id) != null;
    }

    public static boolean entityExists(int id) {
        return entityExists(INSTANCE.level, id);
    }

    @Nullable
    public static Entity getCrosshairTargetEntity() {
        return INSTANCE.hitResult != null && INSTANCE.hitResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) INSTANCE.hitResult).getEntity() : null;
    }

    public static boolean isKeyPressed(int key) {
        return InputConstants.isKeyDown(INSTANCE.getWindow(), key);
    }
}
