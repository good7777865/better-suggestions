package me.shurik.bettersuggestions.client.access;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CustomSuggestionAccessor {
    @Nullable
    Entity better_suggestions$getEntity();
    boolean isEntitySuggestion();
    Component getFormattedText();
    String better_suggestions$getOriginalText();
    List<Component> getMultilineTooltip();
}
