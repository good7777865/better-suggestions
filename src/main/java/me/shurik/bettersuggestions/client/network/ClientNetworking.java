package me.shurik.bettersuggestions.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ClientNetworking {
    public static void send(CustomPacketPayload packet) {
        ClientPlayNetworking.send(packet);
    }
}
