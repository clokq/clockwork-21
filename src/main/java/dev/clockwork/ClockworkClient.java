package dev.clockwork;

import com.mojang.blaze3d.platform.InputConstants;
import dev.clockwork.gui.ClickGuiScreen;
import dev.clockwork.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ClockworkClient implements ClientModInitializer {
	public static KeyMapping OPEN_GUI, PANIC;

	@Override
	public void onInitializeClient() {
		KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("clockwork", "main"));
		OPEN_GUI = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.clockwork.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, cat));
		PANIC = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.clockwork.panic", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_END, cat));

		ModuleManager.init();

		// Optional pack that turns the sun and moon into clock faces (toggle it in Resource Packs).
		try {
			FabricLoader.getInstance().getModContainer("clockwork").ifPresent(c ->
				ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath("clockwork", "clock_sky"), c,
					Component.literal("Clockwork Clock Sky"), PackActivationType.DEFAULT_ENABLED));
		} catch (Throwable t) {
			System.err.println("[Clockwork] Could not register clock sky pack: " + t);
		}

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (OPEN_GUI.consumeClick()) if (dev.clockwork.util.Compat.currentScreen() == null) dev.clockwork.util.Compat.openScreen(new ClickGuiScreen());
			while (PANIC.consumeClick()) ModuleManager.disableAll();
			ModuleManager.tick();
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> ModuleManager.save());
	}
}
