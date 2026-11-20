package dev.clockwork.module.modules;

import dev.clockwork.module.Module;
import dev.clockwork.module.Setting;
import dev.clockwork.util.EspStyle;
import dev.clockwork.util.EspStyle.Target;
import dev.clockwork.util.Projector;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

public class StorageFinder extends Module {
	private final Setting.Bool chests = bool("Chests", true), trapped = bool("Trapped Chests", true),
		barrels = bool("Barrels", true), shulkers = bool("Shulker Boxes", true), droppers = bool("Droppers", true),
		dispensers = bool("Dispensers", false), hoppers = bool("Hoppers", false), enders = bool("Ender Chests", false),
		merge = bool("Merge Double Chests", true);
	private final Setting.Color cChest = color("Chest Color", 0xE8A93A), cTrapped = color("Trapped Color", 0xE5483E),
		cBarrel = color("Barrel Color", 0xA8703C), cShulker = color("Shulker Color", 0xB06BFF),
		cDropper = color("Dropper Color", 0x3FD5E0), cDispenser = color("Dispenser Color", 0xFF8A3D),
		cHopper = color("Hopper Color", 0x9AA0A6), cEnder = color("Ender Color", 0xD04CD0);
	private final Setting.Num scanInterval = num("Scan Interval (ticks)", 10, 2, 60, 1);
	private final EspStyle esp;
	private List<Target> cache = List.of();
	private int ticks;

	public StorageFinder() {
		super("StorageFinder", "Highlights chests, shulkers, droppers and other containers.", Category.RENDER);
		cChest.visible = () -> chests.value; cTrapped.visible = () -> trapped.value; cBarrel.visible = () -> barrels.value;
		cShulker.visible = () -> shulkers.value; cDropper.visible = () -> droppers.value; cDispenser.visible = () -> dispensers.value;
		cHopper.visible = () -> hoppers.value; cEnder.visible = () -> enders.value;
		esp = new EspStyle(s -> add(s));
	}

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
				for (BlockEntity be : ch.getBlockEntities().values()) classify(be, out);
			}
		}
		cache = out;
	}

	private void classify(BlockEntity be, List<Target> out) {
		String label; int rgb;
		if (be instanceof TrappedChestBlockEntity) { if (!trapped.value) return; label = "Trapped Chest"; rgb = cTrapped.rgb(); }
		else if (be instanceof ChestBlockEntity) { if (!chests.value) return; label = "Chest"; rgb = cChest.rgb(); }
		else if (be instanceof BarrelBlockEntity) { if (!barrels.value) return; label = "Barrel"; rgb = cBarrel.rgb(); }
		else if (be instanceof ShulkerBoxBlockEntity) { if (!shulkers.value) return; label = "Shulker"; rgb = cShulker.rgb(); }
		else if (be instanceof DropperBlockEntity) { if (!droppers.value) return; label = "Dropper"; rgb = cDropper.rgb(); }
		else if (be instanceof DispenserBlockEntity) { if (!dispensers.value) return; label = "Dispenser"; rgb = cDispenser.rgb(); }
		else if (be instanceof HopperBlockEntity) { if (!hoppers.value) return; label = "Hopper"; rgb = cHopper.rgb(); }
		else if (be instanceof EnderChestBlockEntity) { if (!enders.value) return; label = "Ender Chest"; rgb = cEnder.rgb(); }
		else return;

		BlockPos pos = be.getBlockPos();
		AABB box = new AABB(pos);
		BlockState st = be.getBlockState();
		if (be instanceof ChestBlockEntity && st.hasProperty(ChestBlock.TYPE) && st.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
			if (!merge.value) { out.add(new Target(box, rgb, label)); return; }
			Direction d = ChestBlock.getConnectedDirection(st);
			BlockPos other = pos.relative(d);
			if (pos.compareTo(other) > 0) return; // the partner emits the merged box
			box = box.minmax(new AABB(other));
			label = "Double " + label;
		}
		out.add(new Target(box, rgb, label));
	}

	@Override public void onHud(GuiGraphicsExtractor g, float pt) {
		if (!Projector.begin(mc, pt)) return;
		esp.render(g, cache);
	}
}
