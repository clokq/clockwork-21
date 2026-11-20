package dev.clockwork.mixin;

import dev.clockwork.module.modules.SkyCustomizer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	@Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
	private void clockwork$sky(Vec3 pos, float pt, CallbackInfoReturnable<Integer> cir) {
		if (SkyCustomizer.colorActive()) cir.setReturnValue(SkyCustomizer.sky(cir.getReturnValue()));
	}
}
