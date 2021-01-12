package net.imperatorstorm.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(GiantEntity.class)
public class GiantEntityStorm extends ZombieEntity {


	protected GiantEntityStorm(EntityType<? extends MobEntity> entityType, World world) {
		super((EntityType<? extends ZombieEntity>) entityType, world);
	}

	@Inject(at = @At("TAIL"), method = "<init>(Ljava/lang/Object;Ljava/lang/Object;)V")
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
	private void init(CallbackInfo DOOR_BREAK_DIFFICULTY_CHECKER){
		BreakDoorGoal breakDoorsGoal = new BreakDoorGoal(this, (Predicate<Difficulty>) DOOR_BREAK_DIFFICULTY_CHECKER);
	}
}