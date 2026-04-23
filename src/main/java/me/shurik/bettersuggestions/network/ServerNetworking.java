package me.shurik.bettersuggestions.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class ServerNetworking {
    public static Collection<ServerPlayer> tracking(Entity entity) {
        return PlayerLookup.tracking(entity);
    }

    public static void broadcastFromEntity(Entity entity, CustomPacketPayload packet) {
        for (ServerPlayer player : tracking(entity)) {
            send(player, packet);
        }
    }

    public static void send(ServerPlayer player, CustomPacketPayload packet) {
        ServerPlayNetworking.send(player, packet);
    }
}
