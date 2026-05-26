package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import com.quickqwek.ciskspawn.entity.AzerionEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AzerionModel extends GeoModel<AzerionEntity> {
    @Override
    public ResourceLocation getModelResource(AzerionEntity e) {
        return ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "geo/azerion.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AzerionEntity e) {
        return ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "textures/entity/azerion.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AzerionEntity e) {
        return ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "animations/azerion.animation.json");
    }
}
