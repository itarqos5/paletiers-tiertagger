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

    public PlayerInfoScreen(Screen parent, PaleTiersApi.PaleTiersData playerData, PlayerSkinWidget skinWidget) {
        super(Text.literal("Player Info"));
        this.parent = parent;
        this.playerData = playerData;
        this.skinWidget = skinWidget;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        this.addDrawableChild(ButtonWidget.builder(
            ScreenTexts.DONE,
            button -> MinecraftClient.getInstance().setScreen(parent)
        ).dimensions(centerX - 100, this.height - 27, 200, 20).build());

        if (this.skinWidget != null) {
            this.skinWidget.setPosition(this.width / 2 - 65, (this.height - 144) / 2);
            this.addDrawableChild(this.skinWidget);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = this.width / 2;
        int startY = (this.height - 56) / 2;
        String selectedGamemode = ModConfig.getSelectedGamemode();
        String tier = playerData.getTierForGamemode(selectedGamemode);

        context.drawCenteredTextWithShadow(this.textRenderer, playerData.playerName + "'s profile", centerX, 20, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Preferred gamemode: " + selectedGamemode, centerX + 5, startY, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Tier: " + tier, centerX + 5, startY + 15, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
