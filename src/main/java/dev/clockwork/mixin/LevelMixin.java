package dev.clockwork.mixin;

import dev.clockwork.module.modules.SkyCustomizer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {
	@Inject(method = "getDayTime", at = @At("RETURN"), cancellable = true, require = 0)
	private void clockwork$time(CallbackInfoReturnable<Long> cir) {
		if (((Object) this) instanceof ClientLevel && SkyCustomizer.timeActive()) {
			cir.setReturnValue(SkyCustomizer.time(cir.getReturnValueJ()));
		}
	}

	@Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true, require = 0)
	private void clockwork$rain(float pt, CallbackInfoReturnable<Float> cir) {
		if (((Object) this) instanceof ClientLevel && SkyCustomizer.noRain()) cir.setReturnValue(0f);
	}

	@Inject(method = "getThunderLevel", at = @At("HEAD"), cancellable = true, require = 0)
	private void clockwork$thunder(float pt, CallbackInfoReturnable<Float> cir) {
		if (((Object) this) instanceof ClientLevel && SkyCustomizer.noRain()) cir.setReturnValue(0f);
	}
}
