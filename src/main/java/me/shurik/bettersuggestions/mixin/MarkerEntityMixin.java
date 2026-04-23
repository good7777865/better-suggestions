package me.shurik.bettersuggestions.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Marker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Marker.class)
public class MarkerEntityMixin {
    @Inject(at = @At("HEAD"), method = "getAddEntityPacket", cancellable = true)
    public void getAddEntityPacket(ServerEntity serverEntity, CallbackInfoReturnable<Packet<ClientGamePacketListener>> info) {
        info.setReturnValue(new ClientboundAddEntityPacket((Marker) (Object) this, serverEntity));
    }
}
