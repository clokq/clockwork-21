package dev.clockwork.module.modules;

import dev.clockwork.module.Module;
import dev.clockwork.module.Setting;

public class Interface extends Module {
	public static Interface I;
	public final Setting.Color accent = color("Accent Color", 0xD9A441);
	public final Setting.Num dim = num("Background Dim %", 45, 0, 90, 5);
	public final Setting.Bool animatedLogo = bool("Animated Logo", true);

	public Interface() {
		super("Interface", "Look and feel of the Clockwork GUI.", Category.MISC);
		I = this;
	}

	@Override public boolean alwaysOn() { return true; }
	@Override public boolean persistent() { return false; }
	public static int accentArgb() { return I == null ? 0xFFD9A441 : I.accent.argb(1.0); }
}
