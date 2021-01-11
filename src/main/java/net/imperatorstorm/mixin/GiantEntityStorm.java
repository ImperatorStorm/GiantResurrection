package net.imperatorstorm.mixin;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GiantEntity.class)
public class GiantEntityStorm {
	@Redirect(at = @At(value = "INVOKE_ASSIGN",target = "Lnet/minecraft/entity/mob/GiantEntity.java"), method = "GiantEntity")
	private void init(CallbackInfo info) {
		class GiantEntity extends ZombieEntity {
			public GiantEntity(EntityType<? extends net.minecraft.entity.mob.GiantEntity> entityType, World world) {
				super(world); }

			protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
				return 10.440001F;
			}


			protected void initAttributes() {
				super.initAttributes();
				this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(100.0D);
				this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.5D);
				this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(50.0D);
			}

			public float getPathfindingFavor(BlockPos pos, WorldView World) {
				return world.getBrightness(pos) - 0.5F;
			}
		}
	}
}
