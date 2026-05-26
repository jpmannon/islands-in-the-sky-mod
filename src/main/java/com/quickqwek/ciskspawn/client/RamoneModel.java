package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.entity.RamoneEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RamoneModel extends GeoModel<RamoneEntity> {
    @Override
    public ResourceLocation getModelResource(RamoneEntity e) {
        return ResourceLocation.fromNamespaceAndPath("ciskspawn", "geo/ramone.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RamoneEntity e) {
        return ResourceLocation.fromNamespaceAndPath("ciskspawn", "textures/entity/ramone.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RamoneEntity e) {
        return ResourceLocation.fromNamespaceAndPath("ciskspawn", "animations/ramone.animation.json");
    }
}
