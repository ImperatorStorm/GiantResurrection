package net.imperatorstorm.mixin;

import net.minecraft.class_5493;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(GiantEntity.class)
public abstract class GiantEntityStorm extends GiantEntity {
	private static final Predicate<Difficulty> DOOR_BREAK_DIFFICULTY_CHECKER;
	private final BreakDoorGoal breakDoorsGoal;

	private boolean canBreakDoors;
	public GiantEntityStorm(EntityType<? extends GiantEntity> entityType, World world) {
		super(entityType, world);
		this.breakDoorsGoal = new BreakDoorGoal(this, DOOR_BREAK_DIFFICULTY_CHECKER);
	}
	protected boolean shouldBreakDoors() {
		return true;
	}

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
	static {
		DOOR_BREAK_DIFFICULTY_CHECKER = (difficulty) -> {
			return difficulty == Difficulty.HARD;
		};
	}
}