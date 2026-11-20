package dev.clockwork.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Every raw draw call in the mod goes through here. If a Minecraft update renames
 * a GuiGraphics method, this is the only file that needs touching.
 */
public final class Draw {
	private Draw() {}

	public static void rect(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int argb) {
		if (x2 > x1 && y2 > y1) g.fill(x1, y1, x2, y2, argb);
	}

	/** Rectangle with chamfered corners (reads as rounded at GUI scale). */
	public static void rrect(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
		rect(g, x + 1, y, x + w - 1, y + h, argb);
		rect(g, x, y + 1, x + w, y + h - 1, argb);
	}

	public static void outline(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int t, int argb) {
		rect(g, x1, y1, x2, y1 + t, argb);
		rect(g, x1, y2 - t, x2, y2, argb);
		rect(g, x1, y1 + t, x1 + t, y2 - t, argb);
		rect(g, x2 - t, y1 + t, x2, y2 - t, argb);
	}

	public static void text(GuiGraphicsExtractor g, String s, int x, int y, int argb) {
		g.text(Minecraft.getInstance().font, s, x, y, argb, true);
	}

	public static int width(String s) { return Minecraft.getInstance().font.width(s); }

	public static String trim(String s, int max) {
		if (width(s) <= max) return s;
		while (s.length() > 1 && width(s + "..") > max) s = s.substring(0, s.length() - 1);
		return s + "..";
	}

	public static void scissor(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2) { g.enableScissor(x1, y1, x2, y2); }
	public static void endScissor(GuiGraphicsExtractor g) { g.disableScissor(); }

	public static void push(GuiGraphicsExtractor g, float tx, float ty, float rot) {
		g.pose().pushMatrix();
		g.pose().translate(tx, ty);
		g.pose().rotate(rot);
	}
	public static void pop(GuiGraphicsExtractor g) { g.pose().popMatrix(); }

	/** A thick line drawn as ONE rotated rectangle, clipped to the screen first. */
	public static void line(GuiGraphicsExtractor g, double x1, double y1, double x2, double y2, int thick, int argb, int sw, int sh) {
		double dx = x2 - x1, dy = y2 - y1;
		double t0 = 0, t1 = 1; // Liang-Barsky clip against the screen
		double[] p = { -dx, dx, -dy, dy };
		double[] q = { x1 + 2, sw + 2 - x1, y1 + 2, sh + 2 - y1 };
		for (int i = 0; i < 4; i++) {
			if (p[i] == 0) { if (q[i] < 0) return; continue; }
			double r = q[i] / p[i];
			if (p[i] < 0) { if (r > t1) return; if (r > t0) t0 = r; }
			else { if (r < t0) return; if (r < t1) t1 = r; }
		}
		double ax = x1 + dx * t0, ay = y1 + dy * t0, bx = x1 + dx * t1, by = y1 + dy * t1;
		double len = Math.hypot(bx - ax, by - ay);
		if (len < 0.5) return;
		push(g, (float) ax, (float) ay, (float) Math.atan2(by - ay, bx - ax));
		int th = Math.max(1, thick);
		g.fill(0, -(th / 2), (int) Math.ceil(len), th - (th / 2), argb);
		pop(g);
	}

	/** Filled disc via scanlines. */
	public static void disc(GuiGraphicsExtractor g, float cx, float cy, float r, int argb) {
		int ri = (int) Math.ceil(r);
		for (int dy = -ri; dy < ri; dy++) {
			double yy = dy + 0.5;
			if (Math.abs(yy) > r) continue;
			double half = Math.sqrt(r * r - yy * yy);
			g.fill(Math.round((float) (cx - half)), Math.round(cy) + dy, Math.round((float) (cx + half)), Math.round(cy) + dy + 1, argb);
		}
	}
}
