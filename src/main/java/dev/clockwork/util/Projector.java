package dev.clockwork.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * World -> screen projection done in pure math from the active camera entity
 * (the player, or the freecam entity). No render-pipeline hooks needed.
 */
public final class Projector {
	public static Vec3 camPos = Vec3.ZERO;
	public static int w, h;
	private static double fx, fy, fz, rx, rz, ux, uy, uz, tanHalf, aspect;
	private static final double NEAR = 0.05;

	private Projector() {}

	public static boolean begin(Minecraft mc, float pt) {
		Entity e = mc.getCameraEntity();
		if (e == null) return false;
		camPos = e.getEyePosition(pt);
		double yaw = Math.toRadians(e.getViewYRot(pt)), pitch = Math.toRadians(e.getViewXRot(pt));
		fx = -Math.sin(yaw) * Math.cos(pitch); fy = -Math.sin(pitch); fz = Math.cos(yaw) * Math.cos(pitch);
		rx = -Math.cos(yaw); rz = -Math.sin(yaw);
		// up = right x forward (right has y = 0)
		ux = -rz * fy; uy = rz * fx - rx * fz; uz = rx * fy;
		w = mc.getWindow().getGuiScaledWidth();
		h = mc.getWindow().getGuiScaledHeight();
		aspect = (double) w / h;
		tanHalf = Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);
		return true;
	}

	private static double[] toCam(double x, double y, double z) {
		double dx = x - camPos.x, dy = y - camPos.y, dz = z - camPos.z;
		return new double[] { dx * rx + dz * rz, dx * ux + dy * uy + dz * uz, dx * fx + dy * fy + dz * fz };
	}

	private static double[] toScreen(double[] c) {
		double nx = c[0] / (c[2] * tanHalf * aspect), ny = c[1] / (c[2] * tanHalf);
		return new double[] { w / 2.0 * (1 + nx), h / 2.0 * (1 - ny) };
	}

	/** Screen position of a point, or null when behind the camera. */
	public static double[] project(Vec3 p) {
		double[] c = toCam(p.x, p.y, p.z);
		return c[2] < NEAR ? null : toScreen(c);
	}

	/** The 12 edges of a box as screen-space segments {x1,y1,x2,y2}, clipped to the near plane. */
	public static List<double[]> edges(AABB b) {
		double[][] cs = new double[8][];
		for (int i = 0; i < 8; i++) {
			cs[i] = toCam((i & 1) == 0 ? b.minX : b.maxX, (i & 2) == 0 ? b.minY : b.maxY, (i & 4) == 0 ? b.minZ : b.maxZ);
		}
		List<double[]> out = new ArrayList<>(12);
		for (int i = 0; i < 8; i++) {
			for (int bit = 1; bit <= 4; bit <<= 1) {
				if ((i & bit) != 0) continue;
				double[] a = cs[i], c = cs[i | bit];
				if (a[2] < NEAR && c[2] < NEAR) continue;
				if (a[2] < NEAR) a = clip(a, c); else if (c[2] < NEAR) c = clip(c, a);
				double[] sa = toScreen(a), sc = toScreen(c);
				out.add(new double[] { sa[0], sa[1], sc[0], sc[1] });
			}
		}
		return out;
	}

	private static double[] clip(double[] behind, double[] front) {
		double t = (NEAR - behind[2]) / (front[2] - behind[2]);
		return new double[] { behind[0] + (front[0] - behind[0]) * t, behind[1] + (front[1] - behind[1]) * t, NEAR };
	}

	/** {minX,minY,maxX,maxY} over segments, or null. */
	public static double[] bounds(List<double[]> segs) {
		if (segs.isEmpty()) return null;
		double x1 = 1e9, y1 = 1e9, x2 = -1e9, y2 = -1e9;
		for (double[] s : segs) {
			x1 = Math.min(x1, Math.min(s[0], s[2])); x2 = Math.max(x2, Math.max(s[0], s[2]));
			y1 = Math.min(y1, Math.min(s[1], s[3])); y2 = Math.max(y2, Math.max(s[1], s[3]));
		}
		return new double[] { x1, y1, x2, y2 };
	}
}
