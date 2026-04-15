package com.paletiers.tiertagger.util;

public final class GamemodeUtil {
    private GamemodeUtil() {}

    public static String normalize(String gamemode) {
        if (gamemode == null || gamemode.isBlank()) {
            return "Sword";
        }
        return switch (gamemode.toLowerCase()) {
            case "sword", "swd" -> "Sword";
            case "crystal", "cpvp" -> "Crystal";
            case "netherite", "nethpot", "neth_pot", "neth-pot", "neth pot" -> "Netherite";
            case "pot", "potion" -> "Potion";
            case "ht cart", "htcart", "ht_cart", "ht-cart" -> "HT Cart";
            case "diapot", "dia pot", "dia_pot", "dia-pot" -> "Diapot";
            case "mace", "macepvp" -> "Mace";
            case "uhc" -> "UHC";
            case "axe", "axepvp" -> "Axe";
            case "smp", "smpkit" -> "SMP";
            case "diasmp" -> "DiaSMP";
            default -> gamemode;
        };
    }
}
