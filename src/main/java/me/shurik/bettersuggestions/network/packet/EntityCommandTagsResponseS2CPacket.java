package me.shurik.bettersuggestions.network.packet;

import me.shurik.bettersuggestions.BetterSuggestionsMod;
import me.shurik.bettersuggestions.utils.ByteBufUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Set;

public record EntityCommandTagsResponseS2CPacket(int entityId, Set<String> commandTags) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityCommandTagsResponseS2CPacket> CODEC
            = StreamCodec.ofMember(EntityCommandTagsResponseS2CPacket::write, EntityCommandTagsResponseS2CPacket::new);
    public static final Type<EntityCommandTagsResponseS2CPacket> ID = new Type<>(BetterSuggestionsMod.id("entity_tags_response"));

    public EntityCommandTagsResponseS2CPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), ByteBufUtils.readSet(buf, RegistryFriendlyByteBuf::readUtf));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(entityId);
        ByteBufUtils.writeCollection(buf, commandTags, RegistryFriendlyByteBuf::writeUtf);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
