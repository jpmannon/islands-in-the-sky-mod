package com.quickqwek.ciskspawn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.quickqwek.ciskspawn.entity.JoelleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class JoelleRenderer extends GeoEntityRenderer<JoelleEntity> {
    public JoelleRenderer(EntityRendererProvider.Context context) {
        super(context, new JoelleModel());
        this.shadowRadius = 0.42F;
    }

    @Override
    public void render(JoelleEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.95F, 0.95F, 0.95F);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
