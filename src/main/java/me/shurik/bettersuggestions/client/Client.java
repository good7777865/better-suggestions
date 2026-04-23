package me.shurik.bettersuggestions.client;

import me.shurik.bettersuggestions.client.access.ClientEntityDataAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

import static me.shurik.bettersuggestions.ModConstants.MOD_ID;

public class Client {
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID + "-client");
    public static final Minecraft INSTANCE = Minecraft.getInstance();
    /**
     * Whether the server has this mod installed.
     */
    public static boolean SERVER_SIDE_PRESENT = false;

    // TODO: make it configurable
    public static final long POLLING_INTERVAL = 3000L;

    /**
     * Track the last entity we highlighted so we can clear it before highlighting another.
     */
    private static WeakReference<Entity> lastHighlightedEntity = new WeakReference<>(null);

    public static void setHighlightedEntity(Entity entity) {
        clearHighlightedEntity();
        if (entity != null) {
            ((ClientEntityDataAccessor) entity).setHighlighted(true);
            lastHighlightedEntity = new WeakReference<>(entity);
        }
    }

    public static void clearHighlightedEntity() {
        Entity previous = lastHighlightedEntity.get();
        if (previous != null) {
            ((ClientEntityDataAccessor) previous).setHighlighted(false);
        }
        lastHighlightedEntity.clear();
    }
}
