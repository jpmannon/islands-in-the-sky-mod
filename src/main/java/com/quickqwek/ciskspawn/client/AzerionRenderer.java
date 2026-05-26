package com.quickqwek.ciskspawn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.quickqwek.ciskspawn.entity.AzerionEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AzerionRenderer extends GeoEntityRenderer<AzerionEntity> {
    public AzerionRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new AzerionModel());
        this.shadowRadius = 0.6F;
    }

    @Override
    public void render(AzerionEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 3.85D, 0.0D);
        poseStack.scale(0.95F, 0.95F, 0.95F);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
