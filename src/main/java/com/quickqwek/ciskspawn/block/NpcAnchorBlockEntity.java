package com.quickqwek.ciskspawn.block;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class NpcAnchorBlockEntity extends BlockEntity {
    private static final String DEFAULT_NPC_TYPE = "mortimer";
    private static final List<String> NPC_TYPES = List.of(
            "mortimer",
            "geera",
            "scoria",
            "azerion_rook",
            "joelle",
            "ramone",
            "velho",
            "tarn",
            "cade",
            "ziiko",
            "agatha"
    );

    private String npcType = DEFAULT_NPC_TYPE;
    @Nullable
    private UUID boundEntityUUID = null;
    private int checkCooldown = 100;

    public NpcAnchorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.NPC_ANCHOR_BE.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NpcAnchorBlockEntity anchor) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        anchor.checkCooldown--;
        if (anchor.checkCooldown > 0) {
            return;
        }

        anchor.checkCooldown = 200;
        anchor.checkSpawn(serverLevel);
    }

    public void cycleNpcType(int direction) {
        int index = NPC_TYPES.indexOf(this.npcType);
        if (index < 0) {
            index = 0;
        }

        int next = Math.floorMod(index + direction, NPC_TYPES.size());
        this.npcType = NPC_TYPES.get(next);
        this.boundEntityUUID = null;
        this.checkCooldown = 1;
        this.setChanged();

        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public String getNpcDisplayName() {
        return switch (this.npcType) {
            case "mortimer" -> "Mortimer";
            case "geera" -> "Geera";
            case "scoria" -> "Scoria";
            case "azerion_rook" -> "Azerion Rook";
            case "joelle" -> "Joelle";
            case "ramone" -> "Ramone";
            case "velho" -> "Velho";
            case "tarn" -> "Tarn";
            case "cade" -> "Cade";
            case "ziiko" -> "Zii-ko";
            case "agatha" -> "Agatha";
            default -> this.npcType;
        };
    }

    private void checkSpawn(ServerLevel level) {
        if (this.boundEntityUUID != null) {
            Entity bound = level.getEntity(this.boundEntityUUID);
            if (bound != null && bound.isAlive() && bound.distanceToSqr(this.worldPosition.getCenter()) <= 64.0D * 64.0D) {
                applyHomePosition(bound);
                return;
            }
            this.boundEntityUUID = null;
        }

        EntityType<?> type = resolveNpcType();
        if (type == null) {
            return;
        }

        AABB searchArea = new AABB(this.worldPosition).inflate(8.0D);
        List<Entity> nearby = level.getEntities((Entity) null, searchArea, entity -> entity.getType() == type);
        if (!nearby.isEmpty()) {
            Entity existing = nearby.getFirst();
            this.boundEntityUUID = existing.getUUID();
            applyHomePosition(existing);
            this.setChanged();
            return;
        }

        Entity entity = type.create(level);
        if (entity == null) {
            return;
        }

        entity.moveTo(
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D,
                this.worldPosition.getZ() + 0.5D,
                entity.getYRot(),
                entity.getXRot()
        );
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }

        applyHomePosition(entity);
        level.addFreshEntity(entity);
        this.boundEntityUUID = entity.getUUID();
        this.setChanged();
    }

    @Nullable
    private EntityType<?> resolveNpcType() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, this.npcType);
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
    }

    private void applyHomePosition(Entity entity) {
        try {
            Method method = entity.getClass().getMethod("setHomePos", BlockPos.class);
            method.invoke(entity, this.worldPosition);
        } catch (NoSuchMethodException ignored) {
            // NPCs can opt in by adding setHomePos(BlockPos); spawning still works before that wiring lands.
        } catch (ReflectiveOperationException exception) {
            CiskSpawnMod.LOG.warn("[CISK] Failed to apply NPC anchor home position to {} at {}: {}",
                    entity.getType().toShortString(), this.worldPosition, exception.getMessage());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("NpcType", this.npcType);
        if (this.boundEntityUUID != null) {
            tag.putUUID("BoundEntityUUID", this.boundEntityUUID);
        }
        tag.putInt("CheckCooldown", this.checkCooldown);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("NpcType")) {
            String loadedType = tag.getString("NpcType").toLowerCase(Locale.ROOT);
            this.npcType = NPC_TYPES.contains(loadedType) ? loadedType : DEFAULT_NPC_TYPE;
        }
        this.boundEntityUUID = tag.hasUUID("BoundEntityUUID") ? tag.getUUID("BoundEntityUUID") : null;
        this.checkCooldown = tag.contains("CheckCooldown") ? tag.getInt("CheckCooldown") : 100;
    }
}
