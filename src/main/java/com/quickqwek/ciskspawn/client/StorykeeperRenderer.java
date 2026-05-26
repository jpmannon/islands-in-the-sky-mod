package com.quickqwek.ciskspawn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.quickqwek.ciskspawn.entity.StorykeeperEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class StorykeeperRenderer extends GeoEntityRenderer<StorykeeperEntity> {
    public StorykeeperRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new StorykeeperModel());
        this.shadowRadius = 0.45F;
    }

    @Override
    public void render(StorykeeperEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        // Standing export offset is tuned so his feet sit on the block.
        // Sitting needs a lower render offset because Create seats already place the passenger above the seat.
        poseStack.translate(0.0D, entity.isVisuallySitting() ? 0.15D : 0.78D, 0.0D);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
