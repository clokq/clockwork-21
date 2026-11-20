package dev.clockwork.module.modules;

import dev.clockwork.module.Module;
import dev.clockwork.module.Setting;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Freecam extends Module {
	public static Freecam I;

	private final Setting.Num speed = num("Speed (b/s)", 15, 1, 80, 1), boost = num("Sprint Boost", 2.5, 1, 8, 0.5),
		smooth = num("Smoothing %", 40, 0, 90, 5);
	private final Setting.Bool freeze = bool("Freeze Player Input", true),
		blockInteract = bool("Block Attacks / Interaction", true);

	private RemotePlayer cam;
	private Vec3 vel = Vec3.ZERO;
	private float yaw, pitch;

	public Freecam() {
		super("Freecam", "Detach the camera and fly around while your body stays put.", Category.RENDER);
		I = this;
	}

	@Override public boolean persistent() { return false; }

	public static boolean active() { return I != null && I.isEnabled() && I.cam != null; }
	public static boolean frozen() { return active() && I.freeze.value; }
	public static boolean blocksInteraction() { return active() && I.blockInteract.value; }

	@Override protected void onEnable() {
		if (mc.player == null || mc.level == null) { setEnabled(false); return; }
		cam = new RemotePlayer(mc.level, mc.player.getGameProfile());
		yaw = mc.player.getYRot(); pitch = mc.player.getXRot();
		cam.setPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
		sync();
		cam.noPhysics = true;
		vel = Vec3.ZERO;
		mc.setCameraEntity(cam);
	}

	@Override protected void onDisable() {
		if (mc.player != null) mc.setCameraEntity(mc.player);
		cam = null;
	}

	private void sync() {
		cam.setYRot(yaw); cam.setXRot(pitch);
		cam.yRotO = yaw; cam.xRotO = pitch;
		cam.xo = cam.getX(); cam.yo = cam.getY(); cam.zo = cam.getZ();
	}

	/** Called from EntityMixin: mouse movement rotates the camera instead of the player. */
	public void turn(double dYaw, double dPitch) {
		yaw += (float) dYaw * 0.15F;
		pitch = Mth.clamp(pitch + (float) dPitch * 0.15F, -90F, 90F);
		if (cam != null) { cam.setYRot(yaw); cam.setXRot(pitch); cam.yRotO = yaw; cam.xRotO = pitch; }
	}

	@Override public void onTick() {
		if (cam == null) return;
		if (mc.player == null || mc.level == null || cam.level() != mc.level) { setEnabled(false); return; }
		var o = mc.options;
		double f = (o.keyUp.isDown() ? 1 : 0) - (o.keyDown.isDown() ? 1 : 0);
		double s = (o.keyLeft.isDown() ? 1 : 0) - (o.keyRight.isDown() ? 1 : 0);
		double v = (o.keyJump.isDown() ? 1 : 0) - (o.keyShift.isDown() ? 1 : 0);
		double yr = Math.toRadians(yaw), pr = Math.toRadians(pitch);
		// movement follows where you look (forward includes pitch), strafe is flat
		Vec3 fwd = new Vec3(-Math.sin(yr) * Math.cos(pr), -Math.sin(pr), Math.cos(yr) * Math.cos(pr));
		Vec3 left = new Vec3(Math.cos(yr), 0, Math.sin(yr));
		Vec3 dir = fwd.scale(f).add(left.scale(s)).add(0, v, 0);
		if (dir.lengthSqr() > 1) dir = dir.normalize();
		double sp = speed.value / 20.0 * (o.keySprint.isDown() ? boost.value : 1.0);
		double a = smooth.value / 100.0;
		vel = vel.scale(a).add(dir.scale(sp * (1 - a)));
		cam.xo = cam.getX(); cam.yo = cam.getY(); cam.zo = cam.getZ();
		cam.setPos(cam.getX() + vel.x, cam.getY() + vel.y, cam.getZ() + vel.z);
		cam.yRotO = yaw; cam.xRotO = pitch;
	}
}
