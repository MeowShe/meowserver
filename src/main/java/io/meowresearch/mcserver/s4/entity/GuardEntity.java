package io.meowresearch.mcserver.s4.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import io.meowresearch.mcserver.s4.MeowServerS4;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class GuardEntity extends PathAwareEntity implements PolymerEntity {

    private double x;
    private double y;
    private double z;

    public GuardEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.setCustomName(Text.literal("The Executor"));
        // The old method of obtaining the coordinates at the time of entity generation.
        // PlayerEntity closestPlayer = world.getClosestPlayer(this, -1);
        // if (closestPlayer != null) {
        //     MeowServerS4.LOGGER.info("Located {}", closestPlayer.getDisplayName());
        //     Vec3d startPos = closestPlayer.getPos();
        //     this.x = startPos.x;
        //     this.y = startPos.y;
        //     this.z = startPos.z;
        // }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    public static DefaultAttributeContainer.Builder createDefenderAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1024.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3 * 1.25)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 18.0)
                .add(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY, 2)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
    }

    // Experimental
    // The new method of obtaining the coordinates at the time of entity generation.
    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F));
        this.goalSelector.add(3, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, MobEntity.class, 1, false, true, (entity) -> entity instanceof Monster && (!isSwimming())));
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getTarget() != null && this.getTarget().isAlive()) {
            this.getNavigation().startMovingTo(this.getTarget(), 1.25);
        } else {
            this.getNavigation().startMovingTo(x, y, z, 1);
        }
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
        return EntityType.VILLAGER;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.x != 0.0F && this.y != 0.0F && this.z != 0.0F) {
            nbt.putDouble("XPos", this.x);
            nbt.putDouble("YPos", this.y);
            nbt.putDouble("ZPos", this.z);
            MeowServerS4.LOGGER.info("Write to NBT: {}, {}, {}", this.x, this.y, this.z);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("XPos")) {
            this.x = nbt.getDouble("XPos");
        }
        if (nbt.contains("YPos")) {
            this.y = nbt.getDouble("YPos");
        }
        if (nbt.contains("ZPos")) {
            this.z = nbt.getDouble("ZPos");
        }
    }
}
