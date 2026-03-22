package com.paletiers.tiertagger.version;

import com.paletiers.tiertagger.PaleTiers;
import net.fabricmc.loader.api.FabricLoader;

public final class VersionSupport {
    public static final MinecraftVersion MIN_SUPPORTED = MinecraftVersion.parse("1.21");
    public static final MinecraftVersion MAX_SUPPORTED = MinecraftVersion.parse("1.21.11");

    private VersionSupport() {}

    public static MinecraftVersion current() {
        String detected = FabricLoader.getInstance()
            .getModContainer("minecraft")
            .orElseThrow(() -> new IllegalStateException("Minecraft mod container not found"))
            .getMetadata()
            .getVersion()
            .getFriendlyString();
        return MinecraftVersion.parse(detected);
    }

    public static boolean isSupported() {
        MinecraftVersion current = current();
        return current.isBetweenInclusive(MIN_SUPPORTED, MAX_SUPPORTED);
    }

    public static void requireSupportedOrThrow() {
        MinecraftVersion current = current();
        if (!current.isBetweenInclusive(MIN_SUPPORTED, MAX_SUPPORTED)) {
            String message = "Unsupported Minecraft version " + current + ". Supported range: "
                + MIN_SUPPORTED + " to " + MAX_SUPPORTED + ".";
            PaleTiers.LOGGER.error(message);
            throw new IllegalStateException(message);
        }

        PaleTiers.LOGGER.info("Detected supported Minecraft version {}", current);
    }
}
