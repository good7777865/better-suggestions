package me.shurik.bettersuggestions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class SpecialRenderer {

    private static void setupRendering(PoseStack matrixStack, Vec3 pos) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        double dx = pos.x - camera.position().x;
        double dy = pos.y - camera.position().y;
        double dz = pos.z - camera.position().z;

        matrixStack.pushPose();
        matrixStack.translate(dx, dy, dz);
    }

    private static void finishRendering(PoseStack matrixStack) {
        matrixStack.popPose();
    }

    public static void renderEntityHighlight(Entity entity, Vector4f color, PoseStack matrixStack, net.minecraft.client.renderer.MultiBufferSource consumers) {
        // Only Interaction gets a queue-based box highlight; Display uses the outline/glow pipeline,
        // Marker/AEC are rendered as items directly from EntityRenderManagerMixin.
        if (entity instanceof Interaction interaction) {
            interactionHighlight(interaction, new Vector4f(0f, 0.8f, 0f, 0.3f), matrixStack, consumers);
        }
    }

    public static void renderBlockHighlight(BlockPos pos, Vector4f color, PoseStack matrixStack, net.minecraft.client.renderer.MultiBufferSource consumers) {
        setupRendering(matrixStack, Vec3.atBottomCenterOf(pos));

        VertexConsumer buffer = consumers.getBuffer(RenderTypes.textBackgroundSeeThrough());

        drawFilledBox(matrixStack, buffer, -0.05d, 0d, -0.05d, 0.05d, 0.1d, 0.05d, color.x, color.y, color.z, color.w);

        finishRendering(matrixStack);
    }

    public static void renderPositionHighlight(Vec3 pos, Vector4f color, PoseStack matrixStack, net.minecraft.client.renderer.MultiBufferSource consumers) {
        setupRendering(matrixStack, pos);
        matrixStack.translate(0F, -0.05F, 0F);

        VertexConsumer buffer = consumers.getBuffer(RenderTypes.textBackgroundSeeThrough());

        drawFilledBox(matrixStack, buffer, -0.05d, 0d, -0.05d, 0.05d, 0.1d, 0.05d, color.x, color.y, color.z, color.w);

        finishRendering(matrixStack);
    }

    public static void interactionHighlight(Interaction interaction, Vector4f color, PoseStack matrixStack, net.minecraft.client.renderer.MultiBufferSource consumers) {
        setupRendering(matrixStack, interaction.position());
        AABB box = interaction.getBoundingBox();

        double halfX = (box.maxX - box.minX) / 2;
        double halfZ = (box.maxZ - box.minZ) / 2;

        VertexConsumer buffer = consumers.getBuffer(RenderTypes.textBackgroundSeeThrough());

        drawFilledBox(matrixStack, buffer, -halfX, 0d, -halfZ, halfX, box.maxY - box.minY, halfZ, color.x, color.y, color.z, color.w);

        finishRendering(matrixStack);
    }

    private static void drawFilledBox(PoseStack poseStack, VertexConsumer vertices,
                                      double minX, double minY, double minZ,
                                      double maxX, double maxY, double maxZ,
                                      float red, float green, float blue, float alpha) {
        Matrix4f positionMatrix = poseStack.last().pose();

        int light = 15728880;

        // Front Face (Z+)
        vertices.addVertex(positionMatrix, (float) minX, (float) minY, (float) maxZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) maxX, (float) minY, (float) maxZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) maxX, (float) maxY, (float) maxZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) minX, (float) maxY, (float) maxZ).setColor(red, green, blue, alpha).setLight(light);

        // Back face (Z-)
        vertices.addVertex(positionMatrix, (float) maxX, (float) minY, (float) minZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) minX, (float) minY, (float) minZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) minX, (float) maxY, (float) minZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) maxX, (float) maxY, (float) minZ).setColor(red, green, blue, alpha).setLight(light);

        // Left face (X-)
        vertices.addVertex(positionMatrix, (float) minX, (float) minY, (float) minZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) minX, (float) minY, (float) maxZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) minX, (float) maxY, (float) maxZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) minX, (float) maxY, (float) minZ).setColor(red, green, blue, alpha).setLight(light);

        // Right face (X+)
        vertices.addVertex(positionMatrix, (float) maxX, (float) minY, (float) maxZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) maxX, (float) minY, (float) minZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) maxX, (float) maxY, (float) minZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) maxX, (float) maxY, (float) maxZ).setColor(red, green, blue, alpha).setLight(light);

        // Top face (Y+)
        vertices.addVertex(positionMatrix, (float) minX, (float) maxY, (float) maxZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) maxX, (float) maxY, (float) maxZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) maxX, (float) maxY, (float) minZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) minX, (float) maxY, (float) minZ).setColor(red, green, blue, alpha).setLight(light);

        // Bottom face (Y-)
        vertices.addVertex(positionMatrix, (float) minX, (float) minY, (float) minZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) maxX, (float) minY, (float) minZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) maxX, (float) minY, (float) maxZ).setColor(red, green, blue, alpha).setLight(light);
        vertices.addVertex(positionMatrix, (float) minX, (float) minY, (float) maxZ).setColor(red, green, blue, alpha).setLight(light);
    }
}
