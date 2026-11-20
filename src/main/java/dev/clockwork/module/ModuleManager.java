package dev.clockwork.module;

import com.google.gson.*;
import dev.clockwork.module.modules.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class ModuleManager {
	public static final List<Module> MODULES = new ArrayList<>();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static Path file;

	private ModuleManager() {}

	public static void init() {
		MODULES.add(new StorageFinder());
		MODULES.add(new SpawnerFinder());
		MODULES.add(new Xray());
		MODULES.add(new Freecam());
		MODULES.add(new Fly());
		MODULES.add(new SkyCustomizer());
		MODULES.add(new AntiVanish());
		MODULES.add(new Interface());
		file = FabricLoader.getInstance().getConfigDir().resolve("clockwork.json");
		load();
		Interface.I.setEnabled(true);

		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("clockwork", "overlay"), (graphics, delta) -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.level == null || mc.player == null) return;
			float pt = delta.getGameTimeDeltaPartialTick(false);
			for (Module m : MODULES) if (m.isEnabled()) m.onHud(graphics, pt);
		});
	}

	public static void tick() {
		for (Module m : MODULES) if (m.isEnabled()) m.onTick();
	}

	public static void disableAll() {
		for (Module m : MODULES) if (!m.alwaysOn()) m.setEnabled(false);
	}

	public static List<Module> byCategory(Module.Category c) {
		List<Module> out = new ArrayList<>();
		for (Module m : MODULES) if (m.category == c) out.add(m);
		return out;
	}

	public static void save() {
		try {
			JsonObject root = new JsonObject();
			for (Module m : MODULES) {
				JsonObject o = new JsonObject();
				o.addProperty("enabled", m.persistent() && m.isEnabled());
				JsonObject s = new JsonObject();
				for (Setting st : m.settings) s.addProperty(st.name, st.save());
				o.add("settings", s);
				root.add(m.name, o);
			}
			Files.writeString(file, GSON.toJson(root));
		} catch (Exception e) { e.printStackTrace(); }
	}

	private static void load() {
		try {
			if (!Files.exists(file)) return;
			JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
			for (Module m : MODULES) {
				if (!root.has(m.name)) continue;
				JsonObject o = root.getAsJsonObject(m.name);
				JsonObject s = o.has("settings") ? o.getAsJsonObject("settings") : new JsonObject();
				for (Setting st : m.settings) if (s.has(st.name)) st.load(s.get(st.name).getAsString());
				if (m.persistent() && o.has("enabled") && o.get("enabled").getAsBoolean()) m.setEnabled(true);
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
}
