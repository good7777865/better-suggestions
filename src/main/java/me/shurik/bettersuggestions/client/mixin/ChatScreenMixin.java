package me.shurik.bettersuggestions.client.mixin;

import me.shurik.bettersuggestions.client.Client;
import me.shurik.bettersuggestions.client.utils.ClientUtils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.shurik.bettersuggestions.ModConstants.CONFIG;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow public TextFieldWidget chatField;
    @Shadow protected String originalChatText;

    @Inject(at = @At("TAIL"), method = "<init>")
    void restoreCommand(CallbackInfo ci) {
        if (Client.storedChatCommand != null) {
            originalChatText = Client.storedChatCommand;
            Client.storedChatCommand = null;
        }
    }

    @Inject(at = @At("HEAD"), method = "removed")
    void storeCommand(CallbackInfo ci) {
        // hasShiftDown() method got removed
        if (ClientUtils.isKeyPressed(GLFW.GLFW_KEY_ESCAPE) && CONFIG.rememberCommandOnEscape && !(ClientUtils.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || ClientUtils.isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT)) && chatField.getText().startsWith("/")) {
            Client.storedChatCommand = chatField.getText();
        }
    }
}