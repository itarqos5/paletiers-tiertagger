package com.paletiers.tiertagger;

/* CREDITS TO:
Central Tierlist
This mod's code is a fork of Central Tierlist's
For their tiertagger mod being open-source
You can find their code at https://github.com/XreatLabz/ctl-tiertagger/
Or in our README
*/

import com.paletiers.tiertagger.util.GamemodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class PaleTiers {
    public static final String MOD_ID = "paletiers";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Set<String> REGISTERED_GAMEMODES = new LinkedHashSet<>();
    private static final Set<String> BLACKLISTED_GAMEMODES = new LinkedHashSet<>();

    static {
        registerAvailableGamemodes(Set.of("Sword", "Crystal", "Netherite", "Potion", "Mace", "UHC", "Axe", "SMP", "DiaSMP"));

        // Disabled gamemodes in settings UI.
        // To enable one later, change its value from `true` to `false`.
        setGamemodeBlacklisted("Crystal", true);
        setGamemodeBlacklisted("Potion", true);
        setGamemodeBlacklisted("UHC", true);
        setGamemodeBlacklisted("Axe", true);
        setGamemodeBlacklisted("SMP", true);
        setGamemodeBlacklisted("DiaSMP", true);
    }

    private PaleTiers() {}

    public static void init() {
        LOGGER.info("PaleTiers initialized");
    }

    public static synchronized void registerAvailableGamemodes(Collection<String> gamemodes) {
        for (String gamemode : gamemodes) {
            if (gamemode == null || gamemode.isBlank()) {
                continue;
            }
            REGISTERED_GAMEMODES.add(GamemodeUtil.normalize(gamemode));
        }
    }

    public static synchronized void setGamemodeBlacklisted(String gamemode, boolean blacklisted) {
        if (gamemode == null || gamemode.isBlank()) {
            return;
        }
        String normalized = GamemodeUtil.normalize(gamemode);
        if (blacklisted) {
            BLACKLISTED_GAMEMODES.add(normalized);
        } else {
            BLACKLISTED_GAMEMODES.remove(normalized);
        }
    }

    public static synchronized String[] getGamemodesForSettings() {
        return REGISTERED_GAMEMODES.stream()
            .filter(gamemode -> !BLACKLISTED_GAMEMODES.contains(gamemode))
            .toArray(String[]::new);
    }
}
