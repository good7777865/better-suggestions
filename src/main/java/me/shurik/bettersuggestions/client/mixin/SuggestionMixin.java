package me.shurik.bettersuggestions.client.mixin;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import me.shurik.bettersuggestions.ModConstants;
import me.shurik.bettersuggestions.client.Client;
import me.shurik.bettersuggestions.client.access.ClientEntityDataAccessor;
import me.shurik.bettersuggestions.client.access.CustomSuggestionAccessor;
import me.shurik.bettersuggestions.client.data.ClientScoreboardValue;
import me.shurik.bettersuggestions.client.utils.ClientUtils;
import me.shurik.bettersuggestions.utils.StringUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Add information to suggestion tooltip.
 */
@Mixin(value = Suggestion.class, remap = false)
public class SuggestionMixin implements CustomSuggestionAccessor {
    @Final
    @Shadow
    private String text;

    private boolean entitySuggestion = false;
    private boolean blockPosSuggestion = false;
    private boolean positionSuggestion = false;

    private Entity suggestions$entity;
    private Vec3 suggestions$position = null;
    private BlockPos suggestions$blockPos = null;

    @Inject(at=@At("RETURN"), method="<init>(Lcom/mojang/brigadier/context/StringRange;Ljava/lang/String;Lcom/mojang/brigadier/Message;)V")
    private void init(final StringRange range, final String text, final Message tooltip, CallbackInfo info) {
        if (text == null) return;

        if (StringUtils.isUUID(text)) {
            entitySuggestion = true;
            suggestions$entity =  ClientUtils.getEntityByUUID(text);
        }
    }

    public boolean isEntitySuggestion() { return entitySuggestion; }
    public boolean isPositionSuggestion() { return positionSuggestion; }
    public boolean isBlockPosSuggestion() { return blockPosSuggestion; }

    public List<Component> getMultilineTooltip() {
        List<Component> tooltip = Lists.newArrayList();

        if (entitySuggestion) {
            Entity entity = better_suggestions$getEntity();
            if (entity != null) {
                if (ModConstants.CONFIG.entitySuggestions.showEntityId) {
                    tooltip.add(StringUtils.formatString(BuiltInRegistries.ENTITY_TYPE.getKey(better_suggestions$getEntity().getType()).toString(), ChatFormatting.GREEN));
                }

                if (ModConstants.CONFIG.entitySuggestions.showEntityUuid) {
                    tooltip.add(Component.translatable("text.suggestion.tooltip.uuid.layout",
                            StringUtils.formatTranslation("text.suggestion.tooltip.uuid", ChatFormatting.AQUA),
                            StringUtils.formatUuidAsIntArray(entity.getUUID())
                    ));
                }

                if (ModConstants.CONFIG.entitySuggestions.showEntityPos) {
                    tooltip.add(Component.translatable("text.suggestion.tooltip.pos.layout",
                            StringUtils.formatTranslation("text.suggestion.tooltip.pos", ChatFormatting.AQUA),
                            StringUtils.formatPos(entity.position())
                    ));
                }

                if (ModConstants.CONFIG.entitySuggestions.showEntityTags) {
                    // Get entity tags
                    Set<String> tags = ((ClientEntityDataAccessor) entity).getClientCommandTags();
                    // No info
                    if (tags == null) {
                        tooltip.add(Component.translatable("text.suggestion.tooltip.entity_tags.loading").withStyle(ChatFormatting.GRAY));
                    }
                    // Check if entity has any tags
                    else if (!tags.isEmpty()) {
                        tooltip.add(Component.translatable("text.suggestion.tooltip.entity_tags.layout",
                                StringUtils.formatTranslation("text.suggestion.tooltip.entity_tags", ChatFormatting.AQUA),
                                StringUtils.formatInt(tags.size(), ChatFormatting.GOLD),
                                StringUtils.formatStrings(tags, ChatFormatting.GREEN)
                        ));
                    }
                }

                if (ModConstants.CONFIG.entitySuggestions.showEntityVehicle && entity.getVehicle() != null) {
                    tooltip.add(Component.translatable("text.suggestion.tooltip.vehicle.layout",
                            StringUtils.formatTranslation("text.suggestion.tooltip.vehicle", ChatFormatting.AQUA),
                            StringUtils.formatString(entity.getVehicle().getName().getString(), ChatFormatting.GREEN)
                    ));
                }

                if (ModConstants.CONFIG.entitySuggestions.showEntityPassengers && !entity.getPassengers().isEmpty()) {
                    // Display number of passengers and their names
                    tooltip.add(Component.translatable("text.suggestion.tooltip.passengers.layout",
                            StringUtils.formatTranslation("text.suggestion.tooltip.passengers", ChatFormatting.AQUA),
                            StringUtils.formatInt(entity.getPassengers().size(), ChatFormatting.GOLD),
                            StringUtils.joinTexts(entity.getPassengers().stream().map(Entity::getName).toList())
                    ));
                }

                if (ModConstants.CONFIG.entitySuggestions.showEntityTeam && entity.getTeam() != null) {
                    tooltip.add(Component.translatable("text.suggestion.tooltip.team.layout",
                            StringUtils.formatTranslation("text.suggestion.tooltip.team", ChatFormatting.AQUA),
                            Component.literal(entity.getTeam().getName()).withStyle(entity.getTeam().getColor())
                    ));
                }

                if (ModConstants.CONFIG.entitySuggestions.showEntityHealth && entity instanceof LivingEntity livingEntity) {
                    tooltip.add(Component.translatable("text.suggestion.tooltip.health.layout",
                            StringUtils.formatTranslation("text.suggestion.tooltip.health", ChatFormatting.AQUA),
                            StringUtils.formatFloat(livingEntity.getHealth(), ChatFormatting.RED),
                            StringUtils.formatFloat(livingEntity.getMaxHealth(), ChatFormatting.RED)
                    ));
                }

                if (ModConstants.CONFIG.entitySuggestions.showEntityScores) {
                    // Get entity tags
                    Set<ClientScoreboardValue> scoreboardValues = ((ClientEntityDataAccessor) entity).getClientScoreboardValues();
                    // No info & no server side
                    if (!Client.SERVER_SIDE_PRESENT) {
                        tooltip.add(StringUtils.formatTranslation("text.suggestion.tooltip.entity_scores.no_server_side", ChatFormatting.GRAY));
                    }
                    else if (scoreboardValues == null) {
                        tooltip.add(StringUtils.formatTranslation("text.suggestion.tooltip.entity_scores.loading", ChatFormatting.GRAY));
                    }
                    // Check if entity has any scores
                    else if (!scoreboardValues.isEmpty()) {
                        tooltip.add(Component.translatable("text.suggestion.tooltip.entity_scores.first_layout",
                                StringUtils.formatTranslation("text.suggestion.tooltip.entity_scores", ChatFormatting.AQUA)
                        ));
                        tooltip.addAll(scoreboardValues.stream().map(value -> Component.translatable("text.suggestion.tooltip.entity_scores.layout",
                                StringUtils.formatString(value.objective(), ChatFormatting.GRAY),
                                StringUtils.formatInt(value.score(), ChatFormatting.YELLOW)
                        )).toList());
                    }
                }

                return tooltip;
            }
        }

        // Default tooltip (only shows up for non-entity suggestions or if entity doesn't exist)
        Message tooltipMessage = ((Suggestion) (Object) this).getTooltip();
        if (tooltipMessage != null) {
            tooltip.add(Component.literal(tooltipMessage.getString()));
        }

        return tooltip;
    }

    @Nullable
    public Entity better_suggestions$getEntity() {
        if (suggestions$entity == null || !ClientUtils.entityExists(suggestions$entity.getId())) {
            suggestions$entity = ClientUtils.getEntityByUUID(text);
        }

        return suggestions$entity;
    }

    public Component getFormattedText() {
        if (entitySuggestion) {
            Entity entity = better_suggestions$getEntity();
            if (entity != null) {
                return Component.translatable("%s (%s)", Component.literal(text), entity.getName());
            }
        }

        return Component.literal(text);
    }

    public String better_suggestions$getOriginalText() {
        return text;
    }
}
