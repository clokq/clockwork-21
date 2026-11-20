package dev.clockwork.module;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class Module {
	public enum Category {
		RENDER("Render"), MOVEMENT("Movement"), WORLD("World"), MISC("Misc");
		public final String label;
		Category(String l) { label = l; }
	}

	public final String name, description;
	public final Category category;
	public final List<Setting> settings = new ArrayList<>();
	public float anim; // GUI toggle animation
	private boolean enabled;

	protected final Minecraft mc = Minecraft.getInstance();

	protected Module(String name, String description, Category category) {
		this.name = name; this.description = description; this.category = category;
	}

	public boolean isEnabled() { return enabled; }
	public void toggle() { setEnabled(!enabled); }
	public void setEnabled(boolean on) {
		if (on == enabled) return;
		enabled = on;
		try { if (on) onEnable(); else onDisable(); } catch (Throwable t) { t.printStackTrace(); }
	}

	/** Whether the on/off state is restored from the config at startup. */
	public boolean persistent() { return true; }
	/** Modules that are always on (hides the toggle in the GUI). */
	public boolean alwaysOn() { return false; }

	protected void onEnable() {}
	protected void onDisable() {}
	public void onTick() {}
	public void onHud(GuiGraphicsExtractor g, float partialTick) {}

	protected <T extends Setting> T add(T s) { settings.add(s); return s; }
	protected Setting.Bool bool(String n, boolean d) { return add(new Setting.Bool(n, d)); }
	protected Setting.Num num(String n, double d, double min, double max, double step) { return add(new Setting.Num(n, d, min, max, step)); }
	protected Setting.Mode mode(String n, String def, String... modes) { return add(new Setting.Mode(n, def, modes)); }
	protected Setting.Color color(String n, int rgb) { return add(new Setting.Color(n, rgb)); }
}
