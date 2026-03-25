package com.paletiers.tiertagger.client.render;

import com.paletiers.tiertagger.api.PaleTiersApi;
import com.paletiers.tiertagger.cache.TierCache;
import com.paletiers.tiertagger.config.ModConfig;
import com.paletiers.tiertagger.util.PlayerNameUtil;
import com.paletiers.tiertagger.version.compat.CompatBridgeFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

public final class TierHudRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private TierHudRenderer() {}

    public static void renderTierAboveNametag(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        String profileName = player != null && player.getGameProfile() != null ? player.getGameProfile().getName() : null;
        renderTierAboveNametag(player, profileName, matrices, vertexConsumers, light);
    }

    public static void renderTierAboveNametag(String playerName, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        renderTierAboveNametag(null, playerName, matrices, vertexConsumers, light);
    }

    private static void renderTierAboveNametag(PlayerEntity player, String playerName, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        String normalizedName = PlayerNameUtil.normalizeForLookup(playerName);
        if (normalizedName == null || normalizedName.isBlank() || !ModConfig.isEnabled()) {
            return;
        }

        if (player != null && player == client.player) {
            return;
        }
        if (player == null && client.player != null && client.player.getGameProfile() != null
            && normalizedName.equalsIgnoreCase(client.player.getGameProfile().getName())) {
            return;
        }

        PaleTiersApi.PaleTiersData tierData = TierCache.getTierData(normalizedName);
        if (tierData == null) {
            return;
        }

        String displayTier;
        String displayGamemode;
        boolean isRetired;

        if (ModConfig.shouldShowHighestTier()) {
            displayTier = tierData.getHighestTier();
            displayGamemode = tierData.getHighestTierGamemode();
            if (displayTier.equals("Unranked") || displayGamemode == null) {
                return;
            }
            isRetired = tierData.isRetired(displayGamemode);
        } else {
            String selectedGamemode = ModConfig.getSelectedGamemode();
            if (!tierData.hasTierForGamemode(selectedGamemode)) {
                return;
            }
            displayTier = tierData.getTierForGamemode(selectedGamemode);
            displayGamemode = selectedGamemode;
            isRetired = tierData.isRetired(selectedGamemode);
        }

        if (isRetired) {
            displayTier = "R" + displayTier;
        }

        Text text = Text.empty();
        if (ModConfig.shouldShowGamemode()) {
            String icon = getGamemodeIcon(displayGamemode);
            if (!icon.isEmpty()) {
                text = Text.literal(icon + " ").styled(s -> s.withColor(0xAAAAAA));
            }
        }

        int tierColor = ModConfig.getTierColor(displayTier);
        text = text.copy().append(Text.literal(displayTier).styled(s -> s.withColor(tierColor)));

        TextRenderer textRenderer = client.textRenderer;
        if (textRenderer == null) {
            return;
        }

        matrices.push();
        try {
            // Lift higher to reduce overlap with other nametag injectors.
            matrices.translate(0.0, -14.0, 0.0);
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            float x = -textRenderer.getWidth(text) / 2.0f;
            int backgroundColor = (int) (0.25F * 255.0F) << 24;

            CompatBridgeFactory.client().drawSeeThroughText(
                textRenderer,
                text,
                x,
                matrix4f,
                vertexConsumers,
                backgroundColor,
                light
            );
        } finally {
            matrices.pop();
        }
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
}
