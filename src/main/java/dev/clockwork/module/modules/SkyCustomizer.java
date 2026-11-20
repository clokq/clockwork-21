package dev.clockwork.module.modules;

import dev.clockwork.module.Module;
import dev.clockwork.module.Setting;
import dev.clockwork.util.Colors;

public class SkyCustomizer extends Module {
	private static SkyCustomizer I;

	private final Setting.Mode timeMode = mode("Time", "Vanilla", "Vanilla", "Fixed", "Speed Multiplier");
	private final Setting.Num hour = num("Hour of Day", 12, 0, 24, 0.25), speedMul = num("Time Speed x", 1, 0, 20, 0.5);
	private final Setting.Mode skyMode = mode("Sky Color", "Vanilla", "Vanilla", "Custom", "Tint", "Rainbow");
	private final Setting.Color skyColor = color("Sky Color Value", 0x7A5CFF);
	private final Setting.Num tint = num("Tint Strength %", 40, 0, 100, 5), rainbowSpeed = num("Rainbow Speed", 1, 0.2, 5, 0.1);
	private final Setting.Bool noRain = bool("No Rain / Thunder", false);

	public SkyCustomizer() {
		super("Sky", "Control time of day, sky color and weather. (Clock sun & moon: enable the 'Clockwork Clock Sky' resource pack.)", Category.WORLD);
		I = this;
		hour.visible = () -> timeMode.is("Fixed");
		speedMul.visible = () -> timeMode.is("Speed Multiplier");
		skyColor.visible = () -> skyMode.is("Custom") || skyMode.is("Tint");
		tint.visible = () -> skyMode.is("Tint");
		rainbowSpeed.visible = () -> skyMode.is("Rainbow");
	}

	public static boolean timeActive() { return I != null && I.isEnabled() && !I.timeMode.is("Vanilla"); }
	public static boolean colorActive() { return I != null && I.isEnabled() && !I.skyMode.is("Vanilla"); }
	public static boolean noRain() { return I != null && I.isEnabled() && I.noRain.value; }

	/** 0 ticks = 06:00, so hour h maps to (h - 6) * 1000. */
	public static long time(long original) {
		if (I.timeMode.is("Fixed")) return Math.floorMod((long) ((I.hour.value - 6.0) * 1000.0), 24000L);
		return (long) (original * I.speedMul.value);
	}

	public static int sky(int original) {
		int orig = original & 0xFFFFFF;
		if (I.skyMode.is("Custom")) return 0xFF000000 | I.skyColor.rgb();
		if (I.skyMode.is("Rainbow")) return 0xFF000000 | Colors.hsb((float) ((System.currentTimeMillis() * I.rainbowSpeed.value / 8000.0) % 1.0), 0.6f, 1f);
		float t = (float) (I.tint.value / 100.0);
		return Colors.lerpArgb(0xFF000000 | orig, 0xFF000000 | I.skyColor.rgb(), t) | 0xFF000000;
	}
}
