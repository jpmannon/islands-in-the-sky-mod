package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import com.quickqwek.ciskspawn.entity.ScoriaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ScoriaModel extends GeoModel<ScoriaEntity> {
    @Override
    public ResourceLocation getModelResource(ScoriaEntity e) {
        return ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "geo/scoria.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ScoriaEntity e) {
        return ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "textures/entity/scoria.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ScoriaEntity e) {
        return ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "animations/scoria.animation.json");
    }
}
