package dev.clockwork.mixin;

import dev.clockwork.module.modules.Fly;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	/** Fly noclip: skip block collision for the local player while enabled. */
	@Inject(method = "aiStep", at = @At("HEAD"), require = 0)
	private void clockwork$noclip(CallbackInfo ci) {
		if (Fly.noclipActive()) ((Entity) (Object) this).noPhysics = true;
	}
}
