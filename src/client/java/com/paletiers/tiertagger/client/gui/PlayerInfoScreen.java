package com.paletiers.tiertagger.client.gui;

import com.paletiers.tiertagger.api.PaleTiersApi;
import com.paletiers.tiertagger.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class PlayerInfoScreen extends Screen {
    private final Screen parent;
    private final PaleTiersApi.PaleTiersData playerData;
    private final PlayerSkinWidget skinWidget;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public PlayerInfoScreen(Screen parent, PaleTiersApi.PaleTiersData playerData, PlayerSkinWidget skinWidget) {
        super(Text.literal("Player Info"));
        this.parent = parent;
        this.playerData = playerData;
        this.skinWidget = skinWidget;
    }

    @Override
    protected void init() {
        this.panelWidth = Math.min(430, this.width - 32);
        this.panelHeight = 190;
        this.panelX = this.width / 2 - this.panelWidth / 2;
        this.panelY = Math.max(12, this.height / 2 - this.panelHeight / 2);

        int centerX = this.width / 2;
        String selectedGamemode = ModConfig.getSelectedGamemode();

        int infoX = this.panelX + 92;
        int infoWidth = this.panelWidth - 108;
        int rowY = this.panelY + 14;

        addDisplayRow(infoX, rowY, infoWidth, playerData.playerName + "'s profile");
        addDisplayRow(infoX, rowY + 24, infoWidth, "Preferred: " + selectedGamemode);
        addDisplayRow(infoX, rowY + 55, infoWidth, "MACE: " + displayTier(playerData.getTierForGamemode("Mace")));
        addDisplayRow(infoX, rowY + 80, infoWidth, "SWORD: " + displayTier(playerData.getTierForGamemode("Sword")));
        addDisplayRow(infoX, rowY + 105, infoWidth, "NETHPOT: " + displayTier(playerData.getTierForGamemode("Netherite")));

        this.addDrawableChild(ButtonWidget.builder(
            ScreenTexts.DONE,
            button -> MinecraftClient.getInstance().setScreen(parent)
        ).dimensions(centerX - 80, this.panelY + this.panelHeight - 26, 160, 20).build());

        if (this.skinWidget != null) {
            this.skinWidget.setPosition(this.panelX + 18, this.panelY + 20);
            this.addDrawableChild(this.skinWidget);
        } else {
            addDisplayRow(this.panelX + 12, this.panelY + 82, 72, "No skin");
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta); // handles renderBackground for you

        // Draw your panel on top
        context.fillGradient(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + this.panelHeight, 0xAA101218, 0xAA0B0D12);
        context.fill(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + 1, 0xFF4C6781);
        context.fill(this.panelX, this.panelY + this.panelHeight - 1, this.panelX + this.panelWidth, this.panelY + this.panelHeight, 0xFF4C6781);
        context.fill(this.panelX, this.panelY, this.panelX + 1, this.panelY + this.panelHeight, 0xFF4C6781);
        context.fill(this.panelX + this.panelWidth - 1, this.panelY, this.panelX + this.panelWidth, this.panelY + this.panelHeight, 0xFF4C6781);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private void addDisplayRow(int x, int y, int width, String text) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(text), button -> {
        }).dimensions(x, y, width, 20).build());
    }

    private static String displayTier(String tier) {
        if (tier == null || tier.isBlank() || "Unranked".equalsIgnoreCase(tier)) {
            return "Not found";
        }
        return tier;
    }
}
