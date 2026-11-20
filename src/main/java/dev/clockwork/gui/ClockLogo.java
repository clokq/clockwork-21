package dev.clockwork.gui;

import java.time.LocalTime;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** The Clockwork logo: a mechanical clock with turning gears, tracking real local time. */
public final class ClockLogo {
	private ClockLogo() {}

	private static void gear(GuiGraphicsExtractor g, float cx, float cy, float r, int teeth, float rot, int body, int hole) {
		int tw = Math.max(1, Math.round(r * 0.28f)), tl = Math.max(2, Math.round(r * 0.3f));
		for (int i = 0; i < teeth; i++) {
			Draw.push(g, cx, cy, rot + (float) (i * Math.PI * 2 / teeth));
			g.fill(Math.round(r - 1), -(tw / 2), Math.round(r) + tl, tw - tw / 2, body);
			Draw.pop(g);
		}
		Draw.disc(g, cx, cy, r, body);
		if (hole != 0) Draw.disc(g, cx, cy, r * 0.35f, hole);
	}

	private static void hand(GuiGraphicsExtractor g, float cx, float cy, float angleCw, float len, int w, int argb) {
		Draw.push(g, cx, cy, (float) (-Math.PI / 2 + angleCw));
		g.fill(-Math.round(len * 0.15f), -(w / 2), Math.round(len), w - w / 2, argb);
		Draw.pop(g);
	}

	public static void draw(GuiGraphicsExtractor g, float cx, float cy, float r, boolean animated) {
		final int brass = 0xFFD9A441, brassDark = 0xFF9C7425, face = 0xFF16161B, steel = 0xFFE8E4DA, red = 0xFFE5483E;
		double t = animated ? System.currentTimeMillis() / 1000.0 : 0;

		// small meshing gear, counter-rotating
		gear(g, cx - r * 0.95f, cy + r * 0.9f, r * 0.42f, 8, (float) (-t * 0.5 + 0.2), brassDark, 0xFF16161B);
		// main case: toothed outer gear + face
		gear(g, cx, cy, r, 14, (float) (t * 0.25), brass, 0);
		Draw.disc(g, cx, cy, r * 0.8f, face);

		// hour ticks
		for (int i = 0; i < 12; i++) {
			float len = i % 3 == 0 ? r * 0.2f : r * 0.1f;
			Draw.push(g, cx, cy, (float) (i * Math.PI / 6));
			int w = i % 3 == 0 ? 2 : 1;
			g.fill(Math.round(r * 0.78f - len), -(w / 2), Math.round(r * 0.74f), w - w / 2, steel);
			Draw.pop(g);
		}

		LocalTime now = LocalTime.now();
		double sec = now.getSecond() + now.getNano() / 1e9, min = now.getMinute() + sec / 60.0, hr = (now.getHour() % 12) + min / 60.0;
		if (!animated) { sec = 30; min = 10; hr = 10 + 10 / 60.0; }
		hand(g, cx, cy, (float) (hr * Math.PI / 6), r * 0.45f, Math.max(2, Math.round(r * 0.14f)), steel);
		hand(g, cx, cy, (float) (min * Math.PI / 30), r * 0.65f, Math.max(1, Math.round(r * 0.09f)), steel);
		hand(g, cx, cy, (float) (sec * Math.PI / 30), r * 0.7f, 1, red);
		Draw.disc(g, cx, cy, Math.max(1.5f, r * 0.1f), brass);
	}
}
