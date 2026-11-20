package dev.clockwork.util;

import net.minecraft.util.Mth;

public final class Colors {
	private Colors() {}

	public static int argb(int rgb, double alpha) {
		int a = (int) Math.round(Mth.clamp(alpha, 0.0, 1.0) * 255.0);
		return (a << 24) | (rgb & 0xFFFFFF);
	}

	public static int hsb(float h, float s, float b) {
		return java.awt.Color.HSBtoRGB(h, s, b) & 0xFFFFFF;
	}

	public static float[] toHsb(int rgb) {
		float[] o = new float[3];
		java.awt.Color.RGBtoHSB((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, o);
		return o;
	}

	public static int lerpArgb(int a, int b, float t) {
		int out = 0;
		for (int shift = 0; shift <= 24; shift += 8) {
			int ca = (a >> shift) & 255, cb = (b >> shift) & 255;
			out |= ((int) (ca + (cb - ca) * t) & 255) << shift;
		}
		return out;
	}
}
