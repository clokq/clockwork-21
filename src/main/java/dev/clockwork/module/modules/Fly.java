package dev.clockwork.module.modules;

import dev.clockwork.module.Module;
import dev.clockwork.module.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class Fly extends Module {
	private static Fly I;

	private final Setting.Mode mode = mode("Mode", "Velocity", "Velocity", "Creative");
	private final Setting.Num speed = num("Speed (b/s)", 10, 1, 40, 0.5), vspeed = num("Vertical Speed (b/s)", 7, 1, 30, 0.5),
		smooth = num("Smoothing %", 35, 0, 90, 5);
	private final Setting.Bool noclip = bool("Noclip (pass through blocks)", false), hover = bool("Hover When Idle", true);

	public Fly() {
		super("Fly", "Fly freely, optionally straight through blocks.", Category.MOVEMENT);
		I = this;
		vspeed.visible = () -> mode.is("Velocity");
		smooth.visible = () -> mode.is("Velocity");
		hover.visible = () -> mode.is("Velocity");
	}

	@Override public boolean persistent() { return false; }

	public static boolean noclipActive() { return I != null && I.isEnabled() && I.noclip.value && !Freecam.active(); }

	@Override protected void onDisable() {
		LocalPlayer p = mc.player;
		if (p == null) return;
		p.setNoGravity(false);
		if (!p.isCreative() && !p.isSpectator()) {
			p.getAbilities().mayfly = false;
			p.getAbilities().flying = false;
		}
		p.getAbilities().setFlyingSpeed(0.05F);
	}

	@Override public void onTick() {
		LocalPlayer p = mc.player;
		if (p == null || Freecam.active()) return;
		if (mode.is("Creative")) {
			p.setNoGravity(false);
			p.getAbilities().mayfly = true;
			p.getAbilities().flying = true;
			p.getAbilities().setFlyingSpeed((float) (speed.value / 20.0 / 4.35)); // vanilla 0.05 ~ 10.9 b/s
			return;
		}
		var o = mc.options;
		double f = (o.keyUp.isDown() ? 1 : 0) - (o.keyDown.isDown() ? 1 : 0);
		double s = (o.keyLeft.isDown() ? 1 : 0) - (o.keyRight.isDown() ? 1 : 0);
		double v = (o.keyJump.isDown() ? 1 : 0) - (o.keyShift.isDown() ? 1 : 0);
		double yr = Math.toRadians(p.getYRot());
		double dx = -Math.sin(yr) * f + Math.cos(yr) * s, dz = Math.cos(yr) * f + Math.sin(yr) * s;
		double len = Math.hypot(dx, dz);
		if (len > 1) { dx /= len; dz /= len; }
		double sp = speed.value / 20.0, vs = vspeed.value / 20.0;
		Vec3 target = new Vec3(dx * sp, v * vs, dz * sp);
		boolean idle = f == 0 && s == 0 && v == 0;
		p.setNoGravity(hover.value || !idle);
		double a = smooth.value / 100.0;
		Vec3 cur = p.getDeltaMovement();
		p.setDeltaMovement(cur.scale(a).add(target.scale(1 - a)));
	}
}
