package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.entity.VelhoEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VelhoModel extends GeoModel<VelhoEntity> {
    @Override
    public ResourceLocation getModelResource(VelhoEntity e) {
        return ResourceLocation.fromNamespaceAndPath("ciskspawn", "geo/velho.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VelhoEntity e) {
        return ResourceLocation.fromNamespaceAndPath("ciskspawn", "textures/entity/velho.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VelhoEntity e) {
        return ResourceLocation.fromNamespaceAndPath("ciskspawn", "animations/velho.animation.json");
    }
}
