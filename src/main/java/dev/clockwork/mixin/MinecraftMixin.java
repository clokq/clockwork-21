package dev.clockwork.mixin;

import dev.clockwork.module.modules.Freecam;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true, require = 0)
	private void clockwork$attack(CallbackInfoReturnable<Boolean> cir) {
		if (Freecam.blocksInteraction()) cir.setReturnValue(false);
	}

	@Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true, require = 0)
	private void clockwork$use(CallbackInfo ci) {
		if (Freecam.blocksInteraction()) ci.cancel();
	}

	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true, require = 0)
	private void clockwork$continue(boolean down, CallbackInfo ci) {
		if (Freecam.blocksInteraction()) ci.cancel();
	}
}
