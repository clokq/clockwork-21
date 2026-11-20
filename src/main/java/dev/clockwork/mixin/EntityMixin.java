package dev.clockwork.mixin;

import dev.clockwork.module.modules.Freecam;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
	/** Freecam: mouse movement turns the camera, not the player. */
	@Inject(method = "turn", at = @At("HEAD"), cancellable = true, require = 0)
	private void clockwork$turn(double yRot, double xRot, CallbackInfo ci) {
		if (Freecam.active() && (Object) this == Minecraft.getInstance().player) {
			Freecam.I.turn(yRot, xRot);
			ci.cancel();
		}
	}
}
