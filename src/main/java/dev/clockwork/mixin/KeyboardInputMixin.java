package dev.clockwork.mixin;

import dev.clockwork.module.modules.Freecam;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
	@Shadow public Input keyPresses;
	@Shadow protected Vec2 moveVector;

	/** Freecam: the real player stands still while the camera flies. */
	@Inject(method = "tick", at = @At("TAIL"), require = 0)
	private void clockwork$freeze(CallbackInfo ci) {
		if (Freecam.frozen()) {
			this.keyPresses = Input.EMPTY;
			this.moveVector = Vec2.ZERO;
		}
	}
}
