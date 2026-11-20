package dev.clockwork.module.modules;

import dev.clockwork.module.Module;
import dev.clockwork.module.Setting;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;

public class Xray extends Module {
	private static Xray I;
	private static final Map<Block, Boolean> CACHE = new ConcurrentHashMap<>();

	private final Setting.Bool coal = bool("Coal", false), iron = bool("Iron", true), copper = bool("Copper", false),
		gold = bool("Gold", true), redstone = bool("Redstone", true), lapis = bool("Lapis", true),
		diamond = bool("Diamond", true), emerald = bool("Emerald", true), quartz = bool("Nether Quartz", false),
		debris = bool("Ancient Debris", true), storage = bool("Containers (Chest/Barrel/Shulker)", true),
		spawners = bool("Spawners", true), fullbright = bool("Fullbright", true);
	private String signature = "";

	public Xray() {
		super("Xray", "Hides ordinary blocks so only selected ores and containers show.", Category.RENDER);
		I = this;
	}

	public static boolean active() { return I != null && I.isEnabled(); }

	public static boolean shouldShow(Block b) {
		return CACHE.computeIfAbsent(b, k -> I != null && I.matches(BuiltInRegistries.BLOCK.getKey(k).getPath()));
	}

	private boolean matches(String p) {
		return (coal.value && p.contains("coal_ore")) || (iron.value && p.contains("iron_ore"))
			|| (copper.value && p.contains("copper_ore")) || (gold.value && p.contains("gold_ore"))
			|| (redstone.value && p.contains("redstone_ore")) || (lapis.value && p.contains("lapis_ore"))
			|| (diamond.value && p.contains("diamond_ore")) || (emerald.value && p.contains("emerald_ore"))
			|| (quartz.value && p.equals("nether_quartz_ore")) || (debris.value && p.equals("ancient_debris"))
			|| (storage.value && (p.contains("chest") || p.equals("barrel") || p.contains("shulker_box")))
			|| (spawners.value && p.equals("spawner"));
	}

	private String sig() {
		StringBuilder sb = new StringBuilder();
		for (Setting s : settings) sb.append(s.save()).append(';');
		return sb.toString();
	}

	private void reload() {
		CACHE.clear();
		if (mc.level != null) dev.clockwork.util.Compat.reloadChunks();
	}

	@Override protected void onEnable() { signature = sig(); reload(); }

	@Override protected void onDisable() {
		reload();
		if (mc.player != null) mc.player.removeEffect(MobEffects.NIGHT_VISION);
	}

	@Override public void onTick() {
		String s = sig();
		if (!s.equals(signature)) { signature = s; reload(); }
		if (fullbright.value && mc.player != null && !mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
			mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, MobEffectInstance.INFINITE_DURATION, 0, false, false));
		} else if (!fullbright.value && mc.player != null) {
			mc.player.removeEffect(MobEffects.NIGHT_VISION);
		}
	}
}
