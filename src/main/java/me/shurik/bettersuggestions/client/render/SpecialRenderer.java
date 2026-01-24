package me.shurik.bettersuggestions.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.InteractionEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
//import org.joml.Quaternionf;
//import org.joml.Vector3f;
import org.joml.Vector4f;

public class SpecialRenderer {

    private static void setupRendering(WorldRenderContext context, Vec3d pos) {
        //noinspection resource
        Camera camera = context.gameRenderer().getCamera();

        double dx = pos.x - camera.getCameraPos().x;
        double dy = pos.y - camera.getCameraPos().y;
        double dz = pos.z - camera.getCameraPos().z;

        context.matrices().push();
        context.matrices().translate(dx, dy, dz);
    }

    private static void finishRendering(WorldRenderContext context) {
        context.matrices().pop();
    }

    public static void renderEntityHighlight(Entity entity, Vector4f color, WorldRenderContext worldContext) {
        if (entity instanceof InteractionEntity interaction) {
            interactionHighlight(interaction, new Vector4f(0f, 0.8f, 0f, 0.3f), worldContext);
        } else if (entity instanceof DisplayEntity displayEntity) {
            displayEntityHighlight(displayEntity, new Vector4f(0.6f, 0f, 0.6f, 0.3f), worldContext);
        }
    }

    public static void renderBlockHighlight(BlockPos pos, Vector4f color, WorldRenderContext worldContext) {
        setupRendering(worldContext, Vec3d.ofBottomCenter(pos));

        VertexConsumer buffer = worldContext.consumers().getBuffer(RenderLayers.textBackgroundSeeThrough());

        drawFilledBox(worldContext.matrices(), buffer, -0.05d, 0d, -0.05d, 0.05d, 0.1d, 0.05d, color.x, color.y, color.z, color.w);

        finishRendering(worldContext);
    }

    public static void renderPositionHighlight(Vec3d pos, Vector4f color, WorldRenderContext worldContext) {
        setupRendering(worldContext, pos);
        worldContext.matrices().translate(0F, -0.05F, 0F);

        VertexConsumer buffer = worldContext.consumers().getBuffer(RenderLayers.textBackgroundSeeThrough());

        drawFilledBox(worldContext.matrices(), buffer, -0.05d, 0d, -0.05d, 0.05d, 0.1d, 0.05d, color.x, color.y, color.z, color.w);

        finishRendering(worldContext);
    }

    public static void interactionHighlight(InteractionEntity interaction, Vector4f color, WorldRenderContext worldContext) {
        setupRendering(worldContext, interaction.getEntityPos());
        Box box = interaction.getBoundingBox();

        //                   getLengthX
        double halfX = (box.maxX - box.minX) / 2;
        //                   getLengthZ
        double halfZ = (box.maxZ - box.minZ) / 2;

        VertexConsumer buffer = worldContext.consumers().getBuffer(RenderLayers.textBackgroundSeeThrough());

        drawFilledBox(worldContext.matrices(), buffer, -halfX, 0d, -halfZ, halfX, box.maxY - box.minY, halfZ, color.x, color.y, color.z, color.w);

        finishRendering(worldContext);
    }

    public static void displayEntityHighlight(DisplayEntity interaction, Vector4f color, WorldRenderContext worldContext) {
        setupRendering(worldContext, interaction.getEntityPos());
        worldContext.matrices().translate(0F, -0.2F, 0F);

        VertexConsumer buffer = worldContext.consumers().getBuffer(RenderLayers.textBackgroundSeeThrough());

        drawFilledBox(worldContext.matrices(), buffer, -0.2d, 0d, -0.2d, 0.2d, 0.4d, 0.2d, color.x, color.y, color.z, color.w);

        finishRendering(worldContext);
    }

    private static void drawFilledBox(MatrixStack matrices, VertexConsumer vertices,
                                      double minX, double minY, double minZ,
                                      double maxX, double maxY, double maxZ,
                                      float red, float green, float blue, float alpha) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        int light = LightmapTextureManager.MAX_LIGHT_COORDINATE; // 0xF000F0

        // Front Face (Z+)
        vertices.vertex(positionMatrix, (float) minX, (float) minY, (float) maxZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) maxX, (float) minY, (float) maxZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) maxX, (float) maxY, (float) maxZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) minX, (float) maxY, (float) maxZ).color(red, green, blue, alpha).light(light);

        // Back face (Z-)
        vertices.vertex(positionMatrix, (float) maxX, (float) minY, (float) minZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) minX, (float) minY, (float) minZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) minX, (float) maxY, (float) minZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) maxX, (float) maxY, (float) minZ).color(red, green, blue, alpha).light(light);

        // Left face (X-)
        vertices.vertex(positionMatrix, (float) minX, (float) minY, (float) minZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) minX, (float) minY, (float) maxZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) minX, (float) maxY, (float) maxZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) minX, (float) maxY, (float) minZ).color(red, green, blue, alpha).light(light);

        // Right face (X+)
        vertices.vertex(positionMatrix, (float) maxX, (float) minY, (float) maxZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) maxX, (float) minY, (float) minZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) maxX, (float) maxY, (float) minZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) maxX, (float) maxY, (float) maxZ).color(red, green, blue, alpha).light(light);

        // Top face (Y+)
        vertices.vertex(positionMatrix, (float) minX, (float) maxY, (float) maxZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) maxX, (float) maxY, (float) maxZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) maxX, (float) maxY, (float) minZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) minX, (float) maxY, (float) minZ).color(red, green, blue, alpha).light(light);

        // Bottom face (Y-)
        vertices.vertex(positionMatrix, (float) minX, (float) minY, (float) minZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) maxX, (float) minY, (float) minZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) maxX, (float) minY, (float) maxZ).color(red, green, blue, alpha).light(light);
        vertices.vertex(positionMatrix, (float) minX, (float) minY, (float) maxZ).color(red, green, blue, alpha).light(light);
    }

//    public static void renderTracer(Entity entity, Vector4f color, WorldRenderContext worldContext) {
//        // Get camera position (player's view)
//        //noinspection resource
//        Camera camera = worldContext.gameRenderer().getCamera();
//
//        // Get entity eye position
//        Vec3d cameraPos = camera.getCameraPos();
//        Vec3d entityPos = entity.getEyePos();
//
//        // Calculate vector from camera to entity
//        Vec3d diff = entityPos.subtract(cameraPos);
//        float length = (float) diff.length();
//
//        // Setup rendering without translation since we'll specify absolute coordinates
//        worldContext.matrices().push();
//
//        // Calculate rotation: We rotate the Z-axis (0,0,1) to point towards the entity
//        // This allows us to draw a straight box along the Z-axis
//        Vector3f startDir = new Vector3f(0, 0, 1);
//        Vector3f targetDir = new Vector3f((float) diff.x, (float) diff.y, (float) diff.z).normalize();
//        Quaternionf rotation = new Quaternionf().rotationTo(startDir, targetDir);
//        worldContext.matrices().multiply(rotation);
//
//        // Using textBackgroundSeeThrough because it ignores depth test (sees through walls)
//        // Since RenderLayers.lines() has depth test enabled, we simulate a line using a thin box
//        VertexConsumer buffer = worldContext.consumers().getBuffer(RenderLayers.textBackgroundSeeThrough());
//
//        float width = 0.02f;
//        float startOffset = 0.5f;
//
//        // Draw the beam from the camera (0,0,0) to the target distance (length) along Z-axis
//        drawFilledBox(
//                worldContext.matrices(),
//                buffer,
//                -width, -width, startOffset,
//                width, width, length,
//                color.x, color.y, color.z, color.w
//        );
//
//        worldContext.matrices().pop();
//    }
}