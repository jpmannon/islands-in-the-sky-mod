package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import com.quickqwek.ciskspawn.entity.GeeraEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GeeraModel extends GeoModel<GeeraEntity> {
    @Override
    public ResourceLocation getModelResource(GeeraEntity e) {
        return ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "geo/geera.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeeraEntity e) {
        return ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "textures/entity/geera.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GeeraEntity e) {
        return ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "animations/geera.animation.json");
    }
}
