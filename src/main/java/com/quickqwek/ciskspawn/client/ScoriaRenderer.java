package com.quickqwek.ciskspawn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.quickqwek.ciskspawn.entity.ScoriaEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ScoriaRenderer extends GeoEntityRenderer<ScoriaEntity> {
    public ScoriaRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ScoriaModel());
        this.shadowRadius = 0.36F;
    }

    @Override
    public void render(ScoriaEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        poseStack.scale(0.72F, 0.72F, 0.72F);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
