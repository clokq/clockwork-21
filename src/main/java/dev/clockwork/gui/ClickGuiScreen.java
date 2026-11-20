package dev.clockwork.gui;

import dev.clockwork.module.Module;
import dev.clockwork.module.ModuleManager;
import dev.clockwork.module.Setting;
import dev.clockwork.module.modules.Interface;
import dev.clockwork.util.Colors;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.IntUnaryOperator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class ClickGuiScreen extends Screen {
	private static final int BG = 0xF2121216, PANEL = 0xFF1A1A20, ROW = 0xFF22222A, ROW_HOV = 0xFF2A2A33,
		TEXT = 0xFFE8E4DA, DIM = 0xFF8A8678, OFF = 0xFF3A3A44;

	private static Module.Category tab = Module.Category.RENDER;
	private static Module selected;

	private record Hit(int x, int y, int w, int h, int cy1, int cy2, Runnable left, Runnable right, DoubleConsumer drag) {}

	private final List<Hit> hits = new ArrayList<>();
	private DoubleConsumer dragging;
	private double scrollMods, scrollSet;
	private int maxMods, maxSet;
	private final long opened = System.currentTimeMillis();

	public ClickGuiScreen() { super(Component.literal("Clockwork")); }

	@Override public boolean isPauseScreen() { return false; }
	@Override public void removed() { ModuleManager.save(); }

	private int accent() { return Interface.accentArgb(); }

	private void hit(int x, int y, int w, int h, int c1, int c2, Runnable l, Runnable r, DoubleConsumer d) {
		hits.add(new Hit(x, y, w, h, c1, c2, l, r, d));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
		hits.clear();
		float open = Math.min(1f, (System.currentTimeMillis() - opened) / 160f);
		g.fill(0, 0, width, height, Colors.argb(0, Interface.I.dim.value / 100.0 * open));

		int W = Math.min(540, width - 8), H = Math.min(310, height - 8);
		int x0 = (width - W) / 2, y0 = (height - H) / 2;
		int acc = accent();

		Draw.rrect(g, x0 - 1, y0 - 1, W + 2, H + 2, Colors.argb(acc & 0xFFFFFF, 0.55));
		Draw.rrect(g, x0, y0, W, H, BG);

		// header
		ClockLogo.draw(g, x0 + 26, y0 + 24, 13, Interface.I.animatedLogo.value);
		Draw.text(g, "CLOCKWORK", x0 + 52, y0 + 13, acc);
		Draw.text(g, "precision utilities", x0 + 52, y0 + 25, DIM);
		g.fill(x0 + 8, y0 + 46, x0 + W - 8, y0 + 47, Colors.argb(acc & 0xFFFFFF, 0.25));

		int top = y0 + 54, bottom = y0 + H - 20;

		// category tabs
		int ty = top;
		for (Module.Category c : Module.Category.values()) {
			boolean sel = c == tab, hov = inside(mx, my, x0 + 8, ty, 96, 22);
			Draw.rrect(g, x0 + 8, ty, 96, 22, sel ? Colors.argb(acc & 0xFFFFFF, 0.22) : hov ? ROW_HOV : PANEL);
			if (sel) Draw.rect(g, x0 + 8, ty + 4, x0 + 10, ty + 18, acc);
			Draw.text(g, c.label, x0 + 18, ty + 7, sel ? acc : TEXT);
			final Module.Category cc = c;
			hit(x0 + 8, ty, 96, 22, 0, height, () -> { tab = cc; selected = null; scrollMods = scrollSet = 0; }, null, null);
			ty += 26;
		}

		int listX = x0 + 112, listW = 176, setX = listX + listW + 8, setW = x0 + W - 8 - setX;
		drawModules(g, mx, my, listX, top, listW, bottom);
		drawSettings(g, mx, my, setX, top, setW, bottom);

		Draw.text(g, "Left-click: toggle   Right-click: settings   RShift/Esc: close   End: panic", x0 + 10, y0 + H - 13, DIM);
	}

	private static boolean inside(int mx, int my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }

	private void drawModules(GuiGraphicsExtractor g, int mx, int my, int x, int top, int w, int bottom) {
		Draw.rrect(g, x, top, w, bottom - top, PANEL);
		List<Module> list = ModuleManager.byCategory(tab);
		if (selected == null && !list.isEmpty()) selected = list.get(0);
		maxMods = Math.max(0, list.size() * 25 + 8 - (bottom - top));
		scrollMods = Mth.clamp(scrollMods, 0, maxMods);
		Draw.scissor(g, x, top, x + w, bottom);
		int y = top + 4 - (int) scrollMods;
		for (Module m : list) {
			boolean hov = inside(mx, my, x + 4, y, w - 8, 22) && my >= top && my < bottom;
			m.anim += ((m.isEnabled() ? 1f : 0f) - m.anim) * 0.3f;
			Draw.rrect(g, x + 4, y, w - 8, 22, selected == m ? 0xFF2D2D38 : hov ? ROW_HOV : ROW);
			if (m.isEnabled()) Draw.rect(g, x + 4, y + 4, x + 6, y + 18, accent());
			Draw.text(g, m.name, x + 12, y + 7, m.isEnabled() ? TEXT : DIM);
			if (!m.alwaysOn()) toggle(g, x + w - 34, y + 6, m.anim);
			final Module mm = m;
			hit(x + 4, y, w - 8, 22, top, bottom, () -> { if (!mm.alwaysOn()) mm.toggle(); selected = mm; scrollSet = 0; }, () -> { selected = mm; scrollSet = 0; }, null);
			y += 25;
		}
		Draw.endScissor(g);
	}

	private void toggle(GuiGraphicsExtractor g, int x, int y, float t) {
		Draw.rrect(g, x, y, 24, 10, Colors.lerpArgb(OFF, accent(), t));
		int kx = x + 1 + Math.round(t * 12);
		Draw.rrect(g, kx, y + 1, 10, 8, 0xFFF4F1EA);
	}

	private void drawSettings(GuiGraphicsExtractor g, int mx, int my, int x, int top, int w, int bottom) {
		Draw.rrect(g, x, top, w, bottom - top, PANEL);
		if (selected == null) return;
		Module m = selected;
		Draw.text(g, m.name, x + 8, top + 7, accent());
		Draw.text(g, Draw.trim(m.description, w - 16), x + 8, top + 19, DIM);
		int areaTop = top + 32;
		g.fill(x + 6, areaTop - 2, x + w - 6, areaTop - 1, 0x22FFFFFF);

		int cx = x + 8, cw = w - 16;
		int y = areaTop + 4 - (int) scrollSet, start = y;
		Draw.scissor(g, x, areaTop, x + w, bottom);
		for (Setting s : m.settings) {
			if (!s.visible.getAsBoolean()) continue;
			if (s instanceof Setting.Bool b) {
				Draw.text(g, b.name, cx, y + 6, TEXT);
				float t = b.value ? 1f : 0f;
				toggle(g, cx + cw - 24, y + 5, t);
				hit(cx, y, cw, 20, areaTop, bottom, () -> b.value = !b.value, null, null);
				y += 22;
			} else if (s instanceof Setting.Num n) {
				Draw.text(g, n.name, cx, y + 2, TEXT);
				String v = n.display();
				Draw.text(g, v, cx + cw - Draw.width(v), y + 2, accent());
				int ty = y + 16;
				Draw.rrect(g, cx, ty, cw, 5, OFF);
				float f = (float) ((n.value - n.min) / (n.max - n.min));
				Draw.rrect(g, cx, ty, Math.max(2, Math.round(cw * f)), 5, accent());
				Draw.rrect(g, cx + Math.round((cw - 4) * f), ty - 2, 4, 9, 0xFFF4F1EA);
				final int tx = cx, tw = cw;
				hit(cx, y + 11, cw, 15, areaTop, bottom, null, null, mxv -> n.set(n.min + Mth.clamp((mxv - tx) / tw, 0, 1) * (n.max - n.min)));
				y += 28;
			} else if (s instanceof Setting.Mode md) {
				Draw.text(g, md.name, cx, y + 6, TEXT);
				String v = "< " + md.get() + " >";
				Draw.text(g, v, cx + cw - Draw.width(v), y + 6, accent());
				hit(cx, y, cw, 20, areaTop, bottom, () -> md.cycle(1), () -> md.cycle(-1), null);
				y += 22;
			} else if (s instanceof Setting.Color c) {
				Draw.text(g, c.name, cx, y + 6, TEXT);
				Draw.rrect(g, cx + cw - 26, y + 3, 26, 13, 0xFF000000 | c.rgb());
				Draw.outline(g, cx + cw - 26, y + 3, cx + cw, y + 16, 1, 0x66FFFFFF);
				int sy = y + 20;
				slider(g, cx, sy, cw, c.h, i -> 0xFF000000 | Colors.hsb(i / 100f, c.s, Math.max(0.6f, c.b)), v -> c.h = v, areaTop, bottom, "Hue");
				slider(g, cx, sy + 14, cw, c.s, i -> 0xFF000000 | Colors.hsb(c.h, i / 100f, c.b), v -> c.s = v, areaTop, bottom, "Sat");
				slider(g, cx, sy + 28, cw, c.b, i -> 0xFF000000 | Colors.hsb(c.h, c.s, i / 100f), v -> c.b = v, areaTop, bottom, "Bri");
				y += 20 + 3 * 14 + 6;
			}
		}
		Draw.endScissor(g);
		maxSet = Math.max(0, (y - start) + 8 - (bottom - areaTop));
		scrollSet = Mth.clamp(scrollSet, 0, maxSet);
	}

	/** Gradient slider for a 0..1 color channel. */
	private void slider(GuiGraphicsExtractor g, int x, int y, int w, float val, IntUnaryOperator colorAt,
		java.util.function.Consumer<Float> set, int c1, int c2, String label) {
		int seg = Math.min(48, w / 3);
		for (int i = 0; i < seg; i++) {
			int xa = x + w * i / seg, xb = x + w * (i + 1) / seg;
			Draw.rect(g, xa, y, xb, y + 9, colorAt.applyAsInt(Math.round(i * 100f / (seg - 1))));
		}
		int kx = x + Math.round((w - 3) * val);
		Draw.rect(g, kx - 1, y - 1, kx + 4, y + 10, 0xFF000000);
		Draw.rect(g, kx, y, kx + 3, y + 9, 0xFFF4F1EA);
		hit(x, y - 2, w, 13, c1, c2, null, null, mxv -> set.accept((float) Mth.clamp((mxv - x) / w, 0, 1)));
	}


	@Override
	public boolean mouseClicked(MouseButtonEvent ev, boolean dbl) {
		int mx = (int) ev.x(), my = (int) ev.y();
		for (int i = hits.size() - 1; i >= 0; i--) {
			Hit h = hits.get(i);
			if (!inside(mx, my, h.x(), h.y(), h.w(), h.h()) || my < h.cy1() || my >= h.cy2()) continue;
			if (h.drag() != null && ev.button() == 0) { dragging = h.drag(); dragging.accept(ev.x()); return true; }
			Runnable r = ev.button() == 0 ? h.left() : ev.button() == 1 ? h.right() : null;
			if (r != null) { r.run(); return true; }
		}
		return super.mouseClicked(ev, dbl);
	}

	@Override public boolean mouseDragged(MouseButtonEvent ev, double dx, double dy) {
		if (dragging != null) { dragging.accept(ev.x()); return true; }
		return super.mouseDragged(ev, dx, dy);
	}

	@Override public boolean mouseReleased(MouseButtonEvent ev) { dragging = null; return super.mouseReleased(ev); }

	@Override
	public boolean mouseScrolled(double mx, double my, double sx, double sy) {
		int W = Math.min(540, width - 8), x0 = (width - W) / 2;
		if (mx < x0 + 112 + 176 + 4) scrollMods = Mth.clamp(scrollMods - sy * 16, 0, maxMods);
		else scrollSet = Mth.clamp(scrollSet - sy * 16, 0, maxSet);
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent ev) {
		if (ev.key() == GLFW.GLFW_KEY_RIGHT_SHIFT || ev.key() == GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; }
		return super.keyPressed(ev);
	}
}
