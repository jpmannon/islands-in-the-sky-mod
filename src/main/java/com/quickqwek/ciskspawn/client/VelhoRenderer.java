package com.quickqwek.ciskspawn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.quickqwek.ciskspawn.entity.VelhoEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VelhoRenderer extends GeoEntityRenderer<VelhoEntity> {
    public VelhoRenderer(EntityRendererProvider.Context context) {
        super(context, new VelhoModel());
        this.shadowRadius = 0.38F;
    }

    @Override
    public void render(VelhoEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.scale(0.95F, 0.95F, 0.95F);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
