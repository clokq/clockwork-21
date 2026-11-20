package dev.clockwork.module.modules;

import dev.clockwork.module.Module;
import dev.clockwork.module.Setting;
import dev.clockwork.util.EspStyle;
import dev.clockwork.util.EspStyle.Target;
import dev.clockwork.util.Projector;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

public class SpawnerFinder extends Module {
	private final Setting.Color color = color("Color", 0xFF3B5C);
	private final Setting.Bool chatAlert = bool("Chat Alert On New Spawner", true);
	private final Setting.Num scanInterval = num("Scan Interval (ticks)", 10, 2, 60, 1);
	private final EspStyle esp;
	private List<Target> cache = List.of();
	private final Set<Long> announced = new HashSet<>();
	private int ticks;

	public SpawnerFinder() {
		super("SpawnerFinder", "Highlights mob spawners, even through walls.", Category.RENDER);
		esp = new EspStyle(s -> add(s));
		esp.range.set(160);
		esp.mode.index = 1; // Wireframe 3D by default
	}

	@Override protected void onEnable() { announced.clear(); }
	@Override protected void onDisable() { cache = List.of(); }

	@Override public void onTick() {
		if (mc.player == null || mc.level == null) { cache = List.of(); return; }
		if (ticks++ % (int) scanInterval.value != 0) return;
		List<Target> out = new ArrayList<>();
		int cr = ((int) esp.range.value >> 4) + 1;
		int pcx = mc.player.getBlockX() >> 4, pcz = mc.player.getBlockZ() >> 4;
		for (int dx = -cr; dx <= cr; dx++) {
			for (int dz = -cr; dz <= cr; dz++) {
				LevelChunk ch = mc.level.getChunkSource().getChunkNow(pcx + dx, pcz + dz);
				if (ch == null) continue;
				for (BlockEntity be : ch.getBlockEntities().values()) {
					if (!(be instanceof SpawnerBlockEntity)) continue;
					BlockPos p = be.getBlockPos();
					out.add(new Target(new AABB(p), color.rgb(), "Spawner"));
					if (chatAlert.value && announced.add(p.asLong())) {
						dev.clockwork.util.Compat.chat(Component.literal("\u00a76[Clockwork] \u00a7fSpawner found at \u00a7e" + p.getX() + " " + p.getY() + " " + p.getZ()));
					}
				}
			}
		}
		cache = out;
	}

	@Override public void onHud(GuiGraphicsExtractor g, float pt) {
		if (!Projector.begin(mc, pt)) return;
		esp.render(g, cache);
	}
}
