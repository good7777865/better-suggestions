package me.shurik.bettersuggestions.network.packet;

import me.shurik.bettersuggestions.BetterSuggestionsMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ModPresenceBeaconPacket implements CustomPacketPayload {
    // Singleton — StreamCodec.unit encodes by reference-equality against this exact instance,
    // so every send site must reuse INSTANCE (a fresh `new ModPresenceBeaconPacket()` triggers
    // IllegalStateException "Can't encode ... expected ...").
    public static final ModPresenceBeaconPacket INSTANCE = new ModPresenceBeaconPacket();
    public static final StreamCodec<RegistryFriendlyByteBuf, ModPresenceBeaconPacket> CODEC = StreamCodec.unit(INSTANCE);
    public static final Type<ModPresenceBeaconPacket> ID = new Type<>(BetterSuggestionsMod.id("mod_presence_beacon"));

    private ModPresenceBeaconPacket() {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
