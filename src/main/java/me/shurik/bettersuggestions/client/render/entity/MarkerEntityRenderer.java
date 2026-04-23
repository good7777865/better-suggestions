package me.shurik.bettersuggestions.client.render.entity;

import me.shurik.bettersuggestions.client.render.state.MarkerEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Marker;

public class MarkerEntityRenderer extends EntityRenderer<Marker, MarkerEntityRenderState> {
    protected MarkerEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public MarkerEntityRenderState createRenderState() {
        return MarkerEntityRenderState.INSTANCE;
    }
}
