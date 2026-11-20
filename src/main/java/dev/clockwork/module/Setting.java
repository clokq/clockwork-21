package dev.clockwork.module;

import dev.clockwork.util.Colors;
import java.util.function.BooleanSupplier;

public abstract class Setting {
	public final String name;
	public BooleanSupplier visible = () -> true;

	protected Setting(String name) { this.name = name; }

	@SuppressWarnings("unchecked")
	public <T extends Setting> T showIf(BooleanSupplier s) { this.visible = s; return (T) this; }

	public abstract String save();
	public abstract void load(String s);

	public static class Bool extends Setting {
		public boolean value;
		public Bool(String n, boolean d) { super(n); value = d; }
		public String save() { return Boolean.toString(value); }
		public void load(String s) { value = Boolean.parseBoolean(s); }
	}

	public static class Num extends Setting {
		public double value, min, max, step;
		public Num(String n, double d, double min, double max, double step) {
			super(n); this.min = min; this.max = max; this.step = step; set(d);
		}
		public void set(double v) {
			v = Math.max(min, Math.min(max, v));
			if (step > 0) v = Math.round(v / step) * step;
			value = v;
		}
		public String display() { return step >= 1 ? Integer.toString((int) value) : String.format("%.2f", value); }
		public String save() { return Double.toString(value); }
		public void load(String s) { try { set(Double.parseDouble(s)); } catch (Exception ignored) {} }
	}

	public static class Mode extends Setting {
		public final String[] modes;
		public int index;
		public Mode(String n, String def, String... modes) {
			super(n); this.modes = modes;
			for (int i = 0; i < modes.length; i++) if (modes[i].equals(def)) index = i;
		}
		public String get() { return modes[index]; }
		public boolean is(String m) { return modes[index].equals(m); }
		public void cycle(int dir) { index = Math.floorMod(index + dir, modes.length); }
		public String save() { return get(); }
		public void load(String s) { for (int i = 0; i < modes.length; i++) if (modes[i].equals(s)) index = i; }
	}

	public static class Color extends Setting {
		public float h, s, b;
		public Color(String n, int rgb) { super(n); float[] f = Colors.toHsb(rgb); h = f[0]; s = f[1]; b = f[2]; }
		public int rgb() { return Colors.hsb(h, s, b); }
		public int argb(double a) { return Colors.argb(rgb(), a); }
		public String save() { return h + "," + s + "," + b; }
		public void load(String str) {
			try { String[] p = str.split(","); h = Float.parseFloat(p[0]); s = Float.parseFloat(p[1]); b = Float.parseFloat(p[2]); }
			catch (Exception ignored) {}
		}
	}
}
