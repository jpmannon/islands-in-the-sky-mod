package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import com.quickqwek.ciskspawn.entity.StorykeeperEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class StorykeeperModel extends GeoModel<StorykeeperEntity> {
    @Override
    public ResourceLocation getModelResource(StorykeeperEntity e) {
        return ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "geo/storykeeper.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StorykeeperEntity e) {
        return ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "textures/entity/storykeeper.png");
    }

    @Override
    public ResourceLocation getAnimationResource(StorykeeperEntity e) {
        return ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "animations/storykeeper.animation.json");
    }
}
