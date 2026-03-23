package com.paletiers.tiertagger.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.paletiers.tiertagger.api.PaleTiersApi;
import com.paletiers.tiertagger.cache.TierCache;
import com.paletiers.tiertagger.config.ModConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PlayerEntity.class, priority = 800)
public abstract class PlayerNametagMixin {

    @Shadow
    public abstract String getNameForScoreboard();

    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Text modifyDisplayName(Text original) {
        if (!ModConfig.isEnabled()) {
            return original;
        }

        String playerName = this.getNameForScoreboard();
        PaleTiersApi.PaleTiersData tierData = TierCache.getTierData(playerName);
        if (tierData == null) {
            return original;
        }

        String displayTier;
        String displayGamemode;
        boolean isRetired;

        if (ModConfig.shouldShowHighestTier()) {
            displayTier = tierData.getHighestTier();
            displayGamemode = tierData.getHighestTierGamemode();
            if (displayTier.equals("Unranked") || displayGamemode == null) {
                return original;
            }
            isRetired = tierData.isRetired(displayGamemode);
        } else {
            String selectedGamemode = ModConfig.getSelectedGamemode();
            if (!tierData.hasTierForGamemode(selectedGamemode)) {
                return original;
            }
            displayTier = tierData.getTierForGamemode(selectedGamemode);
            displayGamemode = selectedGamemode;
            isRetired = tierData.isRetired(selectedGamemode);
        }

        if (isRetired) {
            displayTier = "R" + displayTier;
        }

        Text tierText = Text.empty();
        if (ModConfig.shouldShowGamemode()) {
            String icon = getGamemodeIcon(displayGamemode);
            if (!icon.isEmpty()) {
                tierText = tierText.copy().append(Text.literal(icon + " ").styled(s -> s.withColor(0xAAAAAA)));
            }
        }

        if (ModConfig.isGradientEnabled(displayTier)) {
            int startColor = ModConfig.getTierColor(displayTier);
            int endColor = ModConfig.getGradientColor(displayTier);
            tierText = tierText.copy().append(createGradientText(displayTier, startColor, endColor));
        } else {
            int tierColor = ModConfig.getTierColor(displayTier);
            tierText = tierText.copy().append(Text.literal(displayTier).styled(s -> s.withColor(tierColor)));
        }

        // Cooperative behavior: keep existing name content, append our segment after it.
        return original.copy()
            .append(Text.literal(" | ").styled(s -> s.withColor(0x808080)))
            .append(tierText);
    }

    private static String getGamemodeIcon(String gamemode) {
        return switch (gamemode.toLowerCase()) {
            case "sword" -> "[SWD]";
            case "cpvp", "crystal" -> "[CRY]";
            case "netherite", "nethpot" -> "[NETH]";
            case "pot", "potion" -> "[POT]";
            case "mace", "macepvp" -> "[MACE]";
            case "uhc" -> "[UHC]";
            case "axe", "axepvp" -> "[AXE]";
            case "smp", "smpkit" -> "[SMP]";
            default -> "";
        };
    }

    private static Text createGradientText(String text, int startColor, int endColor) {
        if (text.length() <= 1) {
            return Text.literal(text).styled(s -> s.withColor(startColor));
        }

        Text result = Text.empty();
        int length = text.length();
        for (int i = 0; i < length; i++) {
            float factor = (float) i / (length - 1);

            int r1 = (startColor >> 16) & 0xFF;
            int g1 = (startColor >> 8) & 0xFF;
            int b1 = startColor & 0xFF;
            int r2 = (endColor >> 16) & 0xFF;
            int g2 = (endColor >> 8) & 0xFF;
            int b2 = endColor & 0xFF;

            int r = (int) (r1 + (r2 - r1) * factor);
            int g = (int) (g1 + (g2 - g1) * factor);
            int b = (int) (b1 + (b2 - b1) * factor);

            int interpolatedColor = (r << 16) | (g << 8) | b;
            result = result.copy().append(Text.literal(String.valueOf(text.charAt(i))).styled(s -> s.withColor(interpolatedColor)));
        }
        return result;
    }
}
