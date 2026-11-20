package dev.clockwork.mixin;

import dev.clockwork.module.modules.Xray;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
	/** Xray: only faces of whitelisted blocks are meshed. */
	@Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true, require = 0)
	private static void clockwork$xray(BlockState state, BlockState neighbor, Direction face, CallbackInfoReturnable<Boolean> cir) {
		if (Xray.active()) cir.setReturnValue(Xray.shouldShow(state.getBlock()));
	}
}
