package me.shurik.bettersuggestions.client.event;

import me.shurik.bettersuggestions.client.Client;
import me.shurik.bettersuggestions.client.render.SpecialRendererQueue;
import me.shurik.bettersuggestions.network.packet.ModPresenceBeaconPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;


public class ClientEvents {
    public static void init() {
        // Detect server-side mod presence by receiving the beacon packet
        ClientPlayNetworking.registerGlobalReceiver(ModPresenceBeaconPacket.ID, (packet, context) -> {
            if (!Client.SERVER_SIDE_PRESENT) {
                Client.SERVER_SIDE_PRESENT = true;
                Client.LOGGER.info("Detected mod installed on server");
            }
        });

        // Clear entity tags when disconnecting from server
        // Reset mod presence on server
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            Client.SERVER_SIDE_PRESENT = false;
            SpecialRendererQueue.clearAll();
            Client.clearHighlightedEntity();
        });

        // Drive custom highlight rendering (coordinate highlights and marker/AEC fake highlights).
        // Previously handled by WorldRenderEvents.LAST in 1.21.x; replaced by LevelRenderEvents in 26.1.2.
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(context ->
            SpecialRendererQueue.processQueue(context.poseStack(), context.bufferSource())
        );
    }
}
