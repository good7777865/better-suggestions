package me.shurik.bettersuggestions.network.packet;

import me.shurik.bettersuggestions.BetterSuggestionsMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EntityScoresRequestC2SPacket(int entityId) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityScoresRequestC2SPacket> CODEC
            = StreamCodec.ofMember(EntityScoresRequestC2SPacket::write, EntityScoresRequestC2SPacket::new);
    public static final Type<EntityScoresRequestC2SPacket> ID = new Type<>(BetterSuggestionsMod.id("entity_scores_request"));

    public EntityScoresRequestC2SPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
