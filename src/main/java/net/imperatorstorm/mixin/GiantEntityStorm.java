package net.imperatorstorm.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(GiantEntity.class)
public class GiantEntityStorm extends MobEntity {

	protected GiantEntityStorm(EntityType<? extends MobEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at = @At("TAIL"), method = "<init>(EntityType<? extends ZombieEntity> entityType, World world)V")
	private void init(CallbackInfo DOOR_BREAK_DIFFICULTY_CHECKER){
		BreakDoorGoal breakDoorsGoal = new BreakDoorGoal(this, (Predicate<Difficulty>) DOOR_BREAK_DIFFICULTY_CHECKER);
	}
}