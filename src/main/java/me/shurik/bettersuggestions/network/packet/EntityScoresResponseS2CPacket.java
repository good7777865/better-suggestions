package me.shurik.bettersuggestions.network.packet;

import me.shurik.bettersuggestions.BetterSuggestionsMod;
import me.shurik.bettersuggestions.client.data.ClientScoreboardValue;
import me.shurik.bettersuggestions.utils.ByteBufUtils;
import me.shurik.bettersuggestions.utils.Scoreboards;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public record EntityScoresResponseS2CPacket(int entityId, Collection<? extends Scoreboards.ScoreboardValue> scores) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityScoresResponseS2CPacket> CODEC
            = StreamCodec.ofMember(EntityScoresResponseS2CPacket::write, EntityScoresResponseS2CPacket::new);
    public static final Type<EntityScoresResponseS2CPacket> ID = new Type<>(BetterSuggestionsMod.id("entity_scores_response"));

    public EntityScoresResponseS2CPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), ByteBufUtils.readCollection(buf, ByteBufUtils::readScoreboardValue));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(entityId);
        ByteBufUtils.writeCollection(buf, scores, ByteBufUtils::writeScoreboardValue);
    }

    @Environment(EnvType.CLIENT)
    public Set<ClientScoreboardValue> convertClientScoreboardValue() {
        return scores.stream().map(sv -> (ClientScoreboardValue) sv).collect(Collectors.toSet());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
