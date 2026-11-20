package dev.clockwork.util;

import dev.clockwork.gui.Draw;
import dev.clockwork.module.Setting;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Shared highlight settings + renderer used by StorageFinder and SpawnerFinder. */
public class EspStyle {
	public record Target(AABB box, int rgb, String label) {}

	public final Setting.Mode mode, colorMode, tracerOrigin;
	public final Setting.Num outlineOpacity, fillOpacity, thickness, range, animSpeed;
	public final Setting.Bool tracers, labels, distance;

	/** @param add registers a setting on the owning module and returns it */
	@SuppressWarnings("unchecked")
	public EspStyle(Function<Setting, Setting> add) {
		mode = (Setting.Mode) add.apply(new Setting.Mode("Style", "Box 2D", "Box 2D", "Wireframe 3D", "Corners", "Glow"));
		outlineOpacity = (Setting.Num) add.apply(new Setting.Num("Outline Opacity %", 95, 0, 100, 1));
		fillOpacity = (Setting.Num) add.apply(new Setting.Num("Fill Opacity %", 12, 0, 100, 1));
		thickness = (Setting.Num) add.apply(new Setting.Num("Thickness", 1, 1, 4, 1));
		range = (Setting.Num) add.apply(new Setting.Num("Range (blocks)", 96, 8, 256, 4));
		colorMode = (Setting.Mode) add.apply(new Setting.Mode("Color Mode", "Static", "Static", "Rainbow", "Pulse", "Distance Fade"));
		animSpeed = (Setting.Num) add.apply(new Setting.Num("Animation Speed", 1, 0.2, 5, 0.1));
		animSpeed.visible = () -> !colorMode.is("Static") && !colorMode.is("Distance Fade");
		tracers = (Setting.Bool) add.apply(new Setting.Bool("Tracers", false));
		tracerOrigin = (Setting.Mode) add.apply(new Setting.Mode("Tracer Origin", "Crosshair", "Crosshair", "Bottom"));
		tracerOrigin.visible = () -> tracers.value;
		labels = (Setting.Bool) add.apply(new Setting.Bool("Labels", true));
		distance = (Setting.Bool) add.apply(new Setting.Bool("Show Distance", true));
		distance.visible = () -> labels.value;
	}

	public void render(GuiGraphicsExtractor g, List<Target> targets) {
		Vec3 cam = Projector.camPos;
		long now = System.currentTimeMillis();
		int th = (int) thickness.value, sw = Projector.w, sh = Projector.h;
		double oa = outlineOpacity.value / 100.0, fa = fillOpacity.value / 100.0, rng = range.value;
		double spd = animSpeed.value;

		for (Target t : targets) {
			Vec3 c = t.box().getCenter();
			double d = c.distanceTo(cam);
			if (d > rng) continue;
			List<double[]> edges = Projector.edges(t.box());
			double[] b = Projector.bounds(edges);
			if (b == null) continue;

			int rgb = t.rgb();
			double am = 1.0;
			if (colorMode.is("Rainbow")) rgb = Colors.hsb((float) ((now * spd / 6000.0) % 1.0), 0.8f, 1f);
			else if (colorMode.is("Pulse")) am = 0.55 + 0.45 * Math.sin(now * spd / 250.0);
			else if (colorMode.is("Distance Fade")) am = Mth.clamp(1.0 - d / rng, 0.15, 1.0);
			int oc = Colors.argb(rgb, oa * am), fc = Colors.argb(rgb, fa * am);

			int x1 = (int) Math.round(b[0]), y1 = (int) Math.round(b[1]), x2 = (int) Math.round(b[2]), y2 = (int) Math.round(b[3]);
			boolean onScreen = !(x2 < 0 || y2 < 0 || x1 > sw || y1 > sh);
			if (onScreen) {
				if (fa > 0) Draw.rect(g, Math.max(0, x1), Math.max(0, y1), Math.min(sw, x2), Math.min(sh, y2), fc);
				if (mode.is("Wireframe 3D")) {
					for (double[] e : edges) Draw.line(g, e[0], e[1], e[2], e[3], th, oc, sw, sh);
				} else if (mode.is("Corners")) {
					int cl = Math.max(3, Math.min(x2 - x1, y2 - y1) / 3);
					corner(g, x1, y1, cl, th, oc, 1, 1); corner(g, x2, y1, cl, th, oc, -1, 1);
					corner(g, x1, y2, cl, th, oc, 1, -1); corner(g, x2, y2, cl, th, oc, -1, -1);
				} else if (mode.is("Glow")) {
					for (int i = 0; i < 3; i++) {
						int a = Colors.argb(rgb, oa * am * (i == 0 ? 1.0 : i == 1 ? 0.4 : 0.15));
						Draw.outline(g, x1 - i * th, y1 - i * th, x2 + i * th, y2 + i * th, th, a);
					}
				} else {
					Draw.outline(g, x1, y1, x2, y2, th, oc);
				}
			}

			if (tracers.value) {
				double[] p = Projector.project(c);
				if (p != null) {
					double ox = sw / 2.0, oy = tracerOrigin.is("Bottom") ? sh : sh / 2.0;
					Draw.line(g, ox, oy, p[0], p[1], Math.max(1, th), Colors.argb(rgb, oa * am * 0.8), sw, sh);
				}
			}

			if (labels.value && onScreen) {
				String s = t.label() + (distance.value ? " " + (int) d + "m" : "");
				Draw.text(g, s, (x1 + x2) / 2 - Draw.width(s) / 2, y1 - 10, Colors.argb(rgb, Math.max(0.6, am)));
			}
		}
	}

	private static void corner(GuiGraphicsExtractor g, int x, int y, int len, int th, int argb, int sx, int sy) {
		Draw.rect(g, Math.min(x, x + len * sx), Math.min(y, y + th * sy), Math.max(x, x + len * sx), Math.max(y, y + th * sy), argb);
		Draw.rect(g, Math.min(x, x + th * sx), Math.min(y, y + len * sy), Math.max(x, x + th * sx), Math.max(y, y + len * sy), argb);
	}
}
