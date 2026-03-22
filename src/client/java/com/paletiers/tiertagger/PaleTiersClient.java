package com.paletiers.tiertagger;

import com.paletiers.tiertagger.cache.TierCache;
import com.paletiers.tiertagger.client.gui.PlayerSearchScreen;
import com.paletiers.tiertagger.config.ModConfig;
import com.paletiers.tiertagger.version.ModMenuSupport;
import com.paletiers.tiertagger.version.VersionSupport;
import com.paletiers.tiertagger.version.compat.CompatBridgeFactory;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class PaleTiersClient implements ClientModInitializer {
    private static KeyBinding gamemodeKeybind;
    private static KeyBinding searchKeybind;

    @Override
    public void onInitializeClient() {
        VersionSupport.requireSupportedOrThrow();
        ModMenuSupport.requireCompatibleOrThrow();
        PaleTiers.init();

        ModConfig.init(FabricLoader.getInstance().getConfigDir());
        TierCache.init();

        gamemodeKeybind = KeyBindingHelper.registerKeyBinding(
            CompatBridgeFactory.client().createKeyBinding(
                "key.paletiers.cycle_gamemode",
                GLFW.GLFW_KEY_G,
                "category.paletiers.controls"
            )
        );

        searchKeybind = KeyBindingHelper.registerKeyBinding(
            CompatBridgeFactory.client().createKeyBinding(
                "key.paletiers.search_player",
                GLFW.GLFW_KEY_Y,
                "category.paletiers.controls"
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (gamemodeKeybind.wasPressed()) {
                ModConfig.cycleGamemode();
                if (client.player != null) {
                    String gamemode = ModConfig.getSelectedGamemode();
                    String icon = getGamemodeIcon(gamemode);
                    String message = icon + " Current Gamemode: " + gamemode;
                    client.player.sendMessage(Text.literal(message), true);
                }
            }

            while (searchKeybind.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new PlayerSearchScreen(null));
                }
            }
        });

        PaleTiers.LOGGER.info("PaleTiers client initialized");
    }

    private static String getGamemodeIcon(String gamemode) {
        return switch (gamemode.toLowerCase()) {
            case "sword" -> "\uE801";
            case "crystal" -> "\uE800";
            case "netherite" -> "\uE803";
            case "potion" -> "\uE802";
            case "mace" -> "\uE807";
            case "uhc" -> "\uE804";
            case "axe" -> "\uE805";
            case "smp" -> "\uE806";
            case "diasmp" -> "\uE808";
            default -> "";
        };
    }
}
