package me.shurik.bettersuggestions.network;

import me.shurik.bettersuggestions.ModConstants;
import me.shurik.bettersuggestions.network.packet.*;
import me.shurik.bettersuggestions.utils.Scoreboards;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import me.shurik.bettersuggestions.Server;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ServerPacketHandler {
    private static boolean hasPermissions(ServerPlayer player) {
        return !ModConstants.CONFIG.server.requireOpToRequestData || Server.INSTANCE != null && Server.INSTANCE.getPlayerList().isOp(new net.minecraft.server.players.NameAndId(player.getGameProfile()));
    }

    public static void init() {
        // One of the registrations is likely unnecessary
        PayloadTypeRegistry.clientboundPlay().register(ModPresenceBeaconPacket.ID, ModPresenceBeaconPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ModPresenceBeaconPacket.ID, ModPresenceBeaconPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ModPresenceBeaconPacket.ID, (packet, context) -> {
            // Do nothing
        });

        PayloadTypeRegistry.serverboundPlay().register(EntityCommandTagsRequestC2SPacket.ID, EntityCommandTagsRequestC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EntityCommandTagsRequestC2SPacket.ID, ServerPacketHandler::receiveCommandTagsRequest);

        PayloadTypeRegistry.serverboundPlay().register(EntityScoresRequestC2SPacket.ID, EntityScoresRequestC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EntityScoresRequestC2SPacket.ID, ServerPacketHandler::receiveScoresRequest);

        PayloadTypeRegistry.clientboundPlay().register(EntityCommandTagsResponseS2CPacket.ID, EntityCommandTagsResponseS2CPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EntityScoresResponseS2CPacket.ID, EntityScoresResponseS2CPacket.CODEC);

        // Tell the client we're here so it switches to the custom request path instead of the
        // fallback UUID tricks. Without this the client keeps SERVER_SIDE_PRESENT=false and
        // requestEntityScores() silently no-ops (there's no fallback for scores like tags have),
        // which is why entity scoreboards don't load — including in singleplayer (integrated server).
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            ServerPlayNetworking.send(handler.player, ModPresenceBeaconPacket.INSTANCE)
        );
    }

    private static void receiveCommandTagsRequest(EntityCommandTagsRequestC2SPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        if (hasPermissions(player)) {
            int entityId = packet.entityId();
            Entity entity = player.level().getEntity(entityId);
            if (entity != null) {
                ServerPacketSender.sendEntityCommandTagsResponse(player, entityId, entity.entityTags());
            }
        }
    }

    private static void receiveScoresRequest(EntityScoresRequestC2SPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        if (hasPermissions(player)) {
            int entityId = packet.entityId();
            Entity entity = player.level().getEntity(entityId);
            if (entity != null) {
                ServerPacketSender.sendEntityScoresResponse(player, entityId, Scoreboards.getScores(entity.getScoreboardName()));
            }
        }
    }
}
