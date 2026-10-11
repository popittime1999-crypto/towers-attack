package com.gimr12.towers_attack.entity.custom;

import com.gimr12.towers_attack.entity.GoblinVariant;
import com.gimr12.towers_attack.entity.ModEntities;
import com.gimr12.towers_attack.item.ModItems;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class GoblinEntity extends Animal {
    public final AnimationState idleAnimationState = new AnimationState();

    private static final EntityDataAccessor<Integer> Variant =
            SynchedEntityData.defineId(GoblinEntity.class, EntityDataSerializers.INT);

    public GoblinEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, stack -> stack.is(ModItems.GOLDEN_PEACH), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new AvoidEntityGoal<>(this, Wolf.class, 1F, 1.6, 1.4));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 30d)
                .add(Attributes.MOVEMENT_SPEED, 0.1D)
                .add(Attributes.SCALE, 0.70)
                .add(Attributes.FOLLOW_RANGE, 24D);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        GoblinEntity baby = ModEntities.GOBLIN.get().create(level);
        if (baby != null) {
            GoblinVariant variant = Util.getRandom(GoblinVariant.values(), this.random);
            baby.setVariant(variant);
        }
        return baby;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.GOLDEN_PEACH.get());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            if (!this.idleAnimationState.isStarted()) {
                this.idleAnimationState.start(this.tickCount);
            }
        }
    }

    /* VARIANT */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(Variant, 0);
    }

    private int getTypeVariant() {
        return this.entityData.get(Variant);
    }

    public GoblinVariant getVariant() {
        return GoblinVariant.byId(this.getTypeVariant() & 255);
    }

    private void setVariant(GoblinVariant variant) {
        this.entityData.set(Variant, variant.getId() & 255);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", this.getTypeVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(Variant, compound.getInt("Variant"));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        GoblinVariant variant = Util.getRandom(GoblinVariant.values(), this.random);
        this.setVariant(variant);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    /* SOUNDS */

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ARMADILLO_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ARMADILLO_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ARMADILLO_DEATH;
    }
}