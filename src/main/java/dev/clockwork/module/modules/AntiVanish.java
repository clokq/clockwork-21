package dev.clockwork.module.modules;

import dev.clockwork.gui.Draw;
import dev.clockwork.module.Module;
import dev.clockwork.module.Setting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class AntiVanish extends Module {
	private final Setting.Bool spectators = bool("Detect Spectators (Tab List)", true),
		looking = bool("Detect Players Looking At You", true),
		camera = bool("Detect Spectating You (Camera)", true),
		hudList = bool("HUD List", true);
	private final Setting.Num angle = num("Look Angle (deg)", 12, 2, 45, 1), lookRange = num("Look Range", 24, 4, 96, 1),
		cooldown = num("Alert Cooldown (s)", 10, 1, 60, 1);
	private final Setting.Mode alert = mode("Alert", "Chat + HUD", "Chat + HUD", "Chat", "HUD", "Off");
	private final Setting.Color color = color("Alert Color", 0xFF5555);

	private record Flag(String text, long expires) {}
	private final Map<String, Long> lastAlert = new HashMap<>();
	private final Map<String, Flag> flags = new HashMap<>();


	public AntiVanish() {
		super("AntiVanish", "Warns when someone is spectating, vanished near you, or looking at you.", Category.MISC);
		angle.visible = () -> looking.value; lookRange.visible = () -> looking.value;
	}

	@Override protected void onDisable() { flags.clear(); lastAlert.clear(); }

	@Override public void onTick() {
		if (mc.player == null || mc.level == null || mc.getConnection() == null) return;
		if (mc.player.tickCount % 4 != 0) return;
		Player me = mc.player;

		if (spectators.value) {
			for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
				if (info.getGameMode() != GameType.SPECTATOR) continue;
				if (info.getProfile().id().equals(me.getUUID())) continue;
				flag(info.getProfile().name(), "is in spectator mode");
			}
		}

		Vec3 eye = me.getEyePosition();
		for (Player p : mc.level.players()) {
			if (p == me) continue;
			String name = p.getName().getString();
			double dist = p.distanceTo(me);
			if (camera.value && dist < 1.6 && (p.isSpectator() || p.isInvisible())
				&& Math.abs(Mth.wrapDegrees(p.getYRot() - me.getYRot())) < 12) {
				flag(name, "may be spectating you");
				continue;
			}
			if (looking.value && dist <= lookRange.value) {
				Vec3 toMe = eye.subtract(p.getEyePosition()).normalize();
				double dot = Mth.clamp(p.getViewVector(1.0F).dot(toMe), -1.0, 1.0);
				if (Math.toDegrees(Math.acos(dot)) < angle.value) flag(name, (p.isSpectator() ? "(spectator) " : "") + "is looking at you (" + (int) dist + "m)");
			}
		}
		long now = System.currentTimeMillis();
		flags.values().removeIf(f -> f.expires() < now);
	}

	private void flag(String name, String reason) {
		long now = System.currentTimeMillis();
		String key = name + "|" + reason;
		String text = name + " " + reason;
		boolean hud = alert.is("HUD") || alert.is("Chat + HUD"), chat = alert.is("Chat") || alert.is("Chat + HUD");
		if (hud) flags.put(key, new Flag(text, now + 4000));
		if (chat && now - lastAlert.getOrDefault(key, 0L) > cooldown.value * 1000) {
			dev.clockwork.util.Compat.chat(Component.literal("\u00a76[Clockwork] \u00a7c" + name + " \u00a7f" + reason));
		}
		if (now - lastAlert.getOrDefault(key, 0L) > cooldown.value * 1000) lastAlert.put(key, now);
	}

	@Override public void onHud(GuiGraphicsExtractor g, float pt) {
		if (!hudList.value || flags.isEmpty()) return;
		int y = 28;
		Draw.text(g, "AntiVanish", 6, y, 0xFFD9A441);
		y += 11;
		List<Flag> list = new ArrayList<>(flags.values());
		for (Flag f : list) { Draw.text(g, "\u25cf " + f.text(), 6, y, color.argb(1.0)); y += 10; }
	}
}
