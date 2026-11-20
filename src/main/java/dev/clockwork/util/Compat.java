package dev.clockwork.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Looks up a few Minecraft methods by name at runtime, because Mojang moved/renamed them
 * between versions (screen handling, chat, chunk reload). Safe no-ops if nothing is found.
 */
public final class Compat {
	private Compat() {}

	private static Object invoke(Object target, String[] names, Object... args) {
		if (target == null) return null;
		for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
			for (Method m : c.getDeclaredMethods()) {
				if (m.getParameterCount() != args.length || !matches(m.getName(), names)) continue;
				Class<?>[] pt = m.getParameterTypes();
				boolean ok = true;
				for (int i = 0; i < pt.length && ok; i++) ok = args[i] == null || wrap(pt[i]).isInstance(args[i]);
				if (!ok) continue;
				try { m.setAccessible(true); Object r = m.invoke(target, args); return r == null ? Boolean.TRUE : r; } catch (Throwable ignored) {}
			}
		}
		return null;
	}

	private static boolean matches(String n, String[] names) { for (String s : names) if (s.equals(n)) return true; return false; }

	private static Class<?> wrap(Class<?> p) {
		if (p == boolean.class) return Boolean.class;
		if (p == int.class) return Integer.class;
		if (p == float.class) return Float.class;
		if (p == double.class) return Double.class;
		return p;
	}

	private static Object field(Object target, String name) {
		if (target == null) return null;
		for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
			try { Field f = c.getDeclaredField(name); f.setAccessible(true); return f.get(target); } catch (Throwable ignored) {}
		}
		return null;
	}

	private static Object gui(Minecraft mc) { return field(mc, "gui"); }

	public static Screen currentScreen() {
		Minecraft mc = Minecraft.getInstance();
		for (Object holder : new Object[] { mc, gui(mc) }) {
			Object v = field(holder, "screen");
			if (v == null) v = invoke(holder, new String[] { "screen", "getScreen" });
			if (v instanceof Screen s) return s;
		}
		return null;
	}

	public static void openScreen(Screen screen) {
		Minecraft mc = Minecraft.getInstance();
		if (invoke(mc, new String[] { "setScreen" }, screen) != null) return;
		invoke(gui(mc), new String[] { "setScreen" }, screen);
	}

	public static void chat(Component msg) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			if (invoke(mc.player, new String[] { "sendSystemMessage" }, msg) != null) return;
		}
		Object g = gui(mc);
		Object chat = invoke(g, new String[] { "getChat" });
		invoke(chat, new String[] { "addMessage", "addClientSystemMessage" }, msg);
		System.out.println("[Clockwork] " + msg.getString());
	}

	/** Re-mesh all chunks (needed after changing Xray settings). Falls back to doing nothing. */
	public static void reloadChunks() {
		Minecraft mc = Minecraft.getInstance();
		Object lr = field(mc, "levelRenderer");
		invoke(lr, new String[] { "allChanged", "reload", "allChunksChanged", "markAllChanged" });
	}
}
