package me.shurik.bettersuggestions.client.data;

import com.google.common.collect.Sets;
import me.shurik.bettersuggestions.ModConstants;
import me.shurik.bettersuggestions.client.Client;
import me.shurik.bettersuggestions.client.network.ClientPacketSender;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.world.entity.Entity;

import java.util.Set;

import static me.shurik.bettersuggestions.client.Client.INSTANCE;

public class ClientDataGetter {
    public static final Set<Integer> pendingTagRequests = Sets.newHashSet();
    public static final Set<Integer> pendingScoreRequests = Sets.newHashSet();
    public static void init() {
        // Clear the queue on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            pendingTagRequests.clear();
            pendingScoreRequests.clear();
        });
    }

    public static void requestEntityTags(Entity entity) {
        if (Client.SERVER_SIDE_PRESENT) {
            ClientPacketSender.sendEntityCommandTagsRequest(entity);
        } else {
            INSTANCE.getConnection().send(new ServerboundCommandSuggestionPacket(-entity.getId() - 1_000_000_000, String.format("/tag %s remove ", entity.getStringUUID())));
        }
        if (!pendingTagRequests.add(entity.getId()) && ModConstants.DEBUG) {
            Client.LOGGER.warn("Tags for entity {} ({}) were requested more than once!", entity.getId(), entity.getType().getDescription().getString());
        }
    }

    public static void requestEntityScores(Entity entity) {
        if (Client.SERVER_SIDE_PRESENT) {
            ClientPacketSender.sendEntityScoresRequest(entity);
        }
        if (!pendingScoreRequests.add(entity.getId()) && ModConstants.DEBUG) {
            Client.LOGGER.warn("Scores for entity {} ({}) were requested more than once!", entity.getId(), entity.getType().getDescription().getString());
        }
    }
}
