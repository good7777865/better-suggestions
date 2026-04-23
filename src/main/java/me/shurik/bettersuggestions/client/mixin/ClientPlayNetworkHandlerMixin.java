package me.shurik.bettersuggestions.client.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import me.shurik.bettersuggestions.client.Client;
import me.shurik.bettersuggestions.client.access.ClientEntityDataAccessor;
import me.shurik.bettersuggestions.client.data.ClientDataGetter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Collectors;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "handleCommandSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientSuggestionProvider;completeCustomSuggestions(ILcom/mojang/brigadier/suggestion/Suggestions;)V", shift = At.Shift.BEFORE), cancellable = true)
    void captureSuggestions(ClientboundCommandSuggestionsPacket packet, CallbackInfo info) {
        // Abusing completion packet to store entity id
        // Hopefully no one else uses this or spawns more than a billion entities lol
        if (packet.id() < -1_000_000_000 && packet.id() > -2_000_000_000) {
            // Convert back
            int entityId = -packet.id() - 1_000_000_000;
            // Remove from pending requests
            ClientDataGetter.pendingTagRequests.remove(entityId);
            if (Client.INSTANCE.level == null) { info.cancel(); return; }
            // Get entity
            Entity entity = Client.INSTANCE.level.getEntity(entityId);
            if (entity != null) {
                // Store received tags
                ((ClientEntityDataAccessor) entity).setClientCommandTags(packet.toSuggestions().getList().stream().map(Suggestion::getText).collect(Collectors.toSet()));
            }
            // Stop processing
            info.cancel();
        }
    }
}
