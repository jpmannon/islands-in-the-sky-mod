package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.entity.JoelleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class JoelleModel extends GeoModel<JoelleEntity> {
    @Override
    public ResourceLocation getModelResource(JoelleEntity e) {
        return ResourceLocation.fromNamespaceAndPath("ciskspawn", "geo/joelle.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JoelleEntity e) {
        return ResourceLocation.fromNamespaceAndPath("ciskspawn", "textures/entity/joelle.png");
    }

    @Override
    public ResourceLocation getAnimationResource(JoelleEntity e) {
        return ResourceLocation.fromNamespaceAndPath("ciskspawn", "animations/joelle.animation.json");
    }
}
