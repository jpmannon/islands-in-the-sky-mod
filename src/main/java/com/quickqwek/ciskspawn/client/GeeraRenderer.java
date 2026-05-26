package com.quickqwek.ciskspawn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.quickqwek.ciskspawn.entity.GeeraEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GeeraRenderer extends GeoEntityRenderer<GeeraEntity> {
    public GeeraRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new GeeraModel());
        this.shadowRadius = 0.42F;
    }

    @Override
    public void render(GeeraEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        // Slight render upscale requested after initial Geera test; tune hitbox separately if needed.
        poseStack.scale(1.12F, 1.12F, 1.12F);
        poseStack.translate(0.0D, 0.0D, 0.0D);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
