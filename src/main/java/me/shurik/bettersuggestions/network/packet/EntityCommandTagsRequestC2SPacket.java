package me.shurik.bettersuggestions.network.packet;

import me.shurik.bettersuggestions.BetterSuggestionsMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EntityCommandTagsRequestC2SPacket(int entityId) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityCommandTagsRequestC2SPacket> CODEC
            = StreamCodec.ofMember(EntityCommandTagsRequestC2SPacket::write, EntityCommandTagsRequestC2SPacket::new);
    public static final Type<EntityCommandTagsRequestC2SPacket> ID = new Type<>(BetterSuggestionsMod.id("entity_tags_request"));

    public EntityCommandTagsRequestC2SPacket(RegistryFriendlyByteBuf buf) {
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
