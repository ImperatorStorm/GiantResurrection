package net.imperatorstorm.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.class_5493;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

@Mixin(GiantEntity.class)
class GiantEntity extends HostileEntity {
	private static final TrackedData<Boolean> BABY = null;
	private boolean canBreakDoors;
	private final BreakDoorGoal breakDoorsGoal;
	public GiantEntity(EntityType<? extends net.minecraft.entity.mob.GiantEntity> entityType, World world, BreakDoorGoal breakDoorsGoal) {
		super(entityType, world);
		this.breakDoorsGoal = breakDoorsGoal;
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	public void setCanBreakDoors(boolean canBreakDoors) {
		if (this.shouldBreakDoors() && class_5493.method_30955(this)) {
			if (this.canBreakDoors != canBreakDoors) {
				this.canBreakDoors = canBreakDoors;
				((MobNavigation)this.getNavigation()).setCanPathThroughDoors(canBreakDoors);
				if (canBreakDoors) {
					this.goalSelector.add(1, this.breakDoorsGoal);
				} else {
					this.goalSelector.remove(this.breakDoorsGoal);
				}
			}
		} else if (this.canBreakDoors) {
			this.goalSelector.remove(this.breakDoorsGoal);
			this.canBreakDoors = false;
		}

	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected boolean shouldBreakDoors() {
		return true;
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	public boolean isBaby() {
		return (Boolean)this.getDataTracker().get(BABY);
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	public boolean canBreakDoors() {
		return this.canBreakDoors;
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected void initGoals() {
		this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(8, new LookAroundGoal(this));
		this.initCustomGoals();
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected void initCustomGoals() {
		this.goalSelector.add(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, this::canBreakDoors));
		this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0D));
		this.targetSelector.add(1, (new RevengeGoal(this, new Class[0])).setGroupRevenge(ZombifiedPiglinEntity.class));
		this.targetSelector.add(2, new FollowTargetGoal(this, PlayerEntity.class, true));
		this.targetSelector.add(3, new FollowTargetGoal(this, MerchantEntity.class, false));
		this.targetSelector.add(3, new FollowTargetGoal(this, IronGolemEntity.class, true));
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected void initDataTracker() {
		super.initDataTracker();
		this.getDataTracker().startTracking(BABY, false);
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	public void onTrackedDataSet(TrackedData<?> data) {
		if (BABY.equals(data)) {
			this.calculateDimensions();
		}

		super.onTrackedDataSet(data);
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	public void tick() {
		if (!this.world.isClient && this.isAlive() && !this.isAiDisabled()) {
			super.tick();

		}

	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	public void tickMovement() {
		if (this.isAlive()) {
			boolean bl = this.burnsInDaylight() && this.isAffectedByDaylight();
			if (bl) {
				ItemStack itemStack = this.getEquippedStack(EquipmentSlot.HEAD);
				if (!itemStack.isEmpty()) {
					if (itemStack.isDamageable()) {
						itemStack.setDamage(itemStack.getDamage() + this.random.nextInt(2));
						if (itemStack.getDamage() >= itemStack.getMaxDamage()) {
							this.sendEquipmentBreakStatus(EquipmentSlot.HEAD);
							this.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
						}
					}

					bl = false;
				}

				if (bl) {
					this.setOnFireFor(8);
				}
			}
		}

		super.tickMovement();
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected boolean burnsInDaylight() {
		return true;
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	public boolean damage(DamageSource source, float amount) {
		if (!super.damage(source, amount)) {
			return false;
		} else if (!(this.world instanceof ServerWorld)) {
			return false;
		} else {
			ServerWorld serverWorld = (ServerWorld)this.world;
			LivingEntity livingEntity = this.getTarget();
			if (livingEntity == null && source.getAttacker() instanceof LivingEntity) {
				livingEntity = (LivingEntity)source.getAttacker();
			}

			if (livingEntity != null && this.world.getDifficulty() == Difficulty.HARD && (double)this.random.nextFloat() < this.getAttributeValue(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS) && this.world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
				int i = MathHelper.floor(this.getX());
				int j = MathHelper.floor(this.getY());
				int k = MathHelper.floor(this.getZ());
				ZombieEntity zombieEntity = new ZombieEntity(this.world);

				for(int l = 0; l < 50; ++l) {
					int m = i + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
					int n = j + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
					int o = k + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
					BlockPos blockPos = new BlockPos(m, n, o);
					EntityType<?> entityType = zombieEntity.getType();
					SpawnRestriction.Location location = SpawnRestriction.getLocation(entityType);
					if (SpawnHelper.canSpawn(location, this.world, blockPos, entityType) && SpawnRestriction.canSpawn(entityType, serverWorld, SpawnReason.REINFORCEMENT, blockPos, this.world.random)) {
						zombieEntity.updatePosition((double)m, (double)n, (double)o);
						if (!this.world.isPlayerInRange((double)m, (double)n, (double)o, 7.0D) && this.world.intersectsEntities(zombieEntity) && this.world.isSpaceEmpty(zombieEntity) && !this.world.containsFluid(zombieEntity.getBoundingBox())) {
							zombieEntity.setTarget(livingEntity);
							zombieEntity.initialize(serverWorld, this.world.getLocalDifficulty(zombieEntity.getBlockPos()), SpawnReason.REINFORCEMENT, (EntityData)null, (CompoundTag)null);
							serverWorld.spawnEntityAndPassengers(zombieEntity);
							this.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).addPersistentModifier(new EntityAttributeModifier("Zombie reinforcement caller charge", -0.05000000074505806D, EntityAttributeModifier.Operation.ADDITION));
							zombieEntity.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).addPersistentModifier(new EntityAttributeModifier("Zombie reinforcement callee charge", -0.05000000074505806D, EntityAttributeModifier.Operation.ADDITION));
							break;
						}
					}
				}
			}

			return true;
		}
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	public boolean tryAttack(Entity target) {
		boolean bl = super.tryAttack(target);
		if (bl) {
			float f = this.world.getLocalDifficulty(this.getBlockPos()).getLocalDifficulty();
			if (this.getMainHandStack().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
				target.setOnFireFor(2 * (int)f);
			}
		}

		return bl;
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_ZOMBIE_HURT;
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_ZOMBIE_DEATH;
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected SoundEvent getStepSound() {
		return SoundEvents.ENTITY_ZOMBIE_STEP;
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(this.getStepSound(), 0.15F, 1.0F);
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	public EntityGroup getGroup() {
		return EntityGroup.UNDEAD;
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected void initEquipment(LocalDifficulty difficulty) {
		super.initEquipment(difficulty);
		if (this.random.nextFloat() < (this.world.getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
			int i = this.random.nextInt(3);
			if (i == 0) {
				this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
			} else {
				this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
			}
		}

	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	public void writeCustomDataToTag(CompoundTag tag) {
		super.writeCustomDataToTag(tag);
		tag.putBoolean("IsBaby", this.isBaby());
		tag.putBoolean("CanBreakDoors", this.canBreakDoors());
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	public void readCustomDataFromTag(CompoundTag tag) {
		super.readCustomDataFromTag(tag);
		this.setBaby(tag.getBoolean("IsBaby"));
		this.setCanBreakDoors(tag.getBoolean("CanBreakDoors"));
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	public void onKilledOther(ServerWorld serverWorld, LivingEntity livingEntity) {
		super.onKilledOther(serverWorld, livingEntity);
		if ((serverWorld.getDifficulty() == Difficulty.NORMAL || serverWorld.getDifficulty() == Difficulty.HARD) && livingEntity instanceof VillagerEntity) {
			if (serverWorld.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
				return;
			}

			VillagerEntity villagerEntity = (VillagerEntity)livingEntity;
			ZombieVillagerEntity zombieVillagerEntity = (ZombieVillagerEntity)villagerEntity.method_29243(EntityType.ZOMBIE_VILLAGER, false);
			zombieVillagerEntity.initialize(serverWorld, serverWorld.getLocalDifficulty(zombieVillagerEntity.getBlockPos()), SpawnReason.CONVERSION, new ZombieEntity.ZombieData(false, true), (CompoundTag)null);
			zombieVillagerEntity.setVillagerData(villagerEntity.getVillagerData());
			zombieVillagerEntity.setGossipData((Tag)villagerEntity.getGossip().serialize(NbtOps.INSTANCE).getValue());
			zombieVillagerEntity.setOfferData(villagerEntity.getOffers().toTag());
			zombieVillagerEntity.setXp(villagerEntity.getExperience());
			if (!this.isSilent()) {
				serverWorld.syncWorldEvent((PlayerEntity)null, 1026, this.getBlockPos(), 0);
			}
		}

	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	public boolean canPickupItem(ItemStack stack) {
		return (stack.getItem() != Items.EGG || !this.isBaby() || !this.hasVehicle()) && super.canPickupItem(stack);
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	@Nullable
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
		float f = difficulty.getClampedLocalDifficulty();
		this.setCanPickUpLoot(this.random.nextFloat() < 0.55F * f);

		if (entityData instanceof ZombieEntity.ZombieData) {
			ZombieEntity.ZombieData zombieData = (ZombieEntity.ZombieData)entityData;

			this.setCanBreakDoors(this.shouldBreakDoors() && this.random.nextFloat() < f * 0.1F);
			this.initEquipment(difficulty);
			this.updateEnchantments(difficulty);
		}

		if (this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
			LocalDate localDate = LocalDate.now();
			int i = localDate.get(ChronoField.DAY_OF_MONTH);
			int j = localDate.get(ChronoField.MONTH_OF_YEAR);
			if (j == 10 && i == 31 && this.random.nextFloat() < 0.25F) {
				this.equipStack(EquipmentSlot.HEAD, new ItemStack(this.random.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
				this.armorDropChances[EquipmentSlot.HEAD.getEntitySlotId()] = 0.0F;
			}
		}

		this.applyAttributeModifiers(f);
		return (EntityData)entityData;
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected void initAttributes() {
		this.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).setBaseValue(this.random.nextDouble() * 0.10000000149011612D);
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected void applyAttributeModifiers(float chanceMultiplier) {
		this.initAttributes();
		this.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).addPersistentModifier(new EntityAttributeModifier("Random spawn bonus", this.random.nextDouble() * 0.05000000074505806D, EntityAttributeModifier.Operation.ADDITION));
		double d = this.random.nextDouble() * 1.5D * (double)chanceMultiplier;
		if (d > 1.0D) {
			this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).addPersistentModifier(new EntityAttributeModifier("Random zombie-spawn bonus", d, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
		}

		if (this.random.nextFloat() < chanceMultiplier * 0.05F) {
			this.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).addPersistentModifier(new EntityAttributeModifier("Leader zombie bonus", this.random.nextDouble() * 0.25D + 0.5D, EntityAttributeModifier.Operation.ADDITION));
			this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(new EntityAttributeModifier("Leader zombie bonus", this.random.nextDouble() * 3.0D + 1.0D, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
			this.setCanBreakDoors(this.shouldBreakDoors());
		}
	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
		super.dropEquipment(source, lootingMultiplier, allowDrops);
		Entity entity = source.getAttacker();
		if (entity instanceof CreeperEntity) {
			CreeperEntity creeperEntity = (CreeperEntity)entity;
			if (creeperEntity.shouldDropHead()) {
				ItemStack itemStack = this.getSkull();
				if (!itemStack.isEmpty()) {
					creeperEntity.onHeadDropped();
					this.dropStack(itemStack);
				}
			}
		}

	}
	@Inject(at=@At("HEAD"), method = ("Lnet/net/minecraft/entity/mob/GiantEntity.java"))
	protected ItemStack getSkull() {
		return new ItemStack(Items.ZOMBIE_HEAD);
	}
}