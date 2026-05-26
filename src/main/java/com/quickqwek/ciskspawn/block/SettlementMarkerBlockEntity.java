package com.quickqwek.ciskspawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SettlementMarkerBlockEntity extends BlockEntity {
    private String settlementId = "";

    public SettlementMarkerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SETTLEMENT_MARKER_BE.get(), pos, blockState);
    }

    public String getSettlementId() {
        return this.settlementId;
    }

    public void setSettlementId(String settlementId) {
        this.settlementId = settlementId;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("SettlementId", this.settlementId);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.settlementId = tag.getString("SettlementId");
    }
}
