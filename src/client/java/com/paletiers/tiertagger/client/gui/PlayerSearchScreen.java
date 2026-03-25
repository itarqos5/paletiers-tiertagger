package com.paletiers.tiertagger.client.gui;

import com.paletiers.tiertagger.PaleTiers;
import com.paletiers.tiertagger.api.PaleTiersApi;
import com.paletiers.tiertagger.cache.TierCache;
import com.paletiers.tiertagger.client.util.SkinLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class PlayerSearchScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget searchField;
    private ButtonWidget searchButton;
    private boolean isSearching = false;
    private String errorMessage = null;

    public PlayerSearchScreen(Screen parent) {
        super(Text.literal("Search Players"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.searchField = new TextFieldWidget(this.textRenderer, centerX - 150, centerY - 30, 300, 20, Text.literal("Search"));
        this.searchField.setPlaceholder(Text.literal("Enter player name..."));
        this.searchField.setMaxLength(16);
        this.searchField.setEditable(true);
        this.addDrawableChild(this.searchField);
        this.setInitialFocus(this.searchField);

        this.searchButton = ButtonWidget.builder(Text.literal("Search"), button -> performSearch())
            .dimensions(centerX - 75, centerY + 5, 150, 20)
            .build();
        this.addDrawableChild(this.searchButton);
    }

    private void performSearch() {
        String query = this.searchField.getText().trim();
        if (query.isEmpty()) {
            this.errorMessage = "Please enter a player name";
            return;
        }
        if (this.isSearching) {
            return;
        }

        this.isSearching = true;
        this.errorMessage = null;
        this.searchButton.active = false;

        openPlayerProfile(query);
    }

    private void openPlayerProfile(String playerName) {
        if (this.client == null) {
            PaleTiers.LOGGER.error("[Search] Client is null");
            return;
        }

        CompletableFuture<PlayerSkinWidget> skinWidgetFuture = SkinLoader.loadSkinAndCreateWidget(playerName, this.client);
        CompletableFuture<PaleTiersApi.PaleTiersData> dataFuture = TierCache.fetchNow(playerName, true);

        CompletableFuture.allOf(dataFuture, skinWidgetFuture).thenRun(() -> {
            PaleTiersApi.PaleTiersData data = dataFuture.join();
            PlayerSkinWidget skinWidget = skinWidgetFuture.join();
            this.client.execute(() -> {
                this.isSearching = false;
                this.searchButton.active = true;
                if (data != null && !data.getAllTiers().isEmpty()) {
                    this.client.setScreen(new PlayerInfoScreen(this, data, skinWidget));
                } else {
                    this.errorMessage = "Player not found in the PaleTiers API";
                    showPlayerNotFoundToast(playerName);
                }
            });
        }).exceptionally(throwable -> {
            PaleTiers.LOGGER.error("[Search] Failed to load {}: {}", playerName, throwable.getMessage());
            if (this.client != null) {
                this.client.execute(() -> {
                    this.isSearching = false;
                    this.searchButton.active = true;
                    this.errorMessage = "Failed to load player data";
                });
            }
            return null;
        });
    }

    private void showPlayerNotFoundToast(String playerName) {
        if (this.client == null) {
            return;
        }
        SystemToast.show(
            this.client.getToastManager(),
            SystemToast.Type.PERIODIC_NOTIFICATION,
            Text.literal("PaleTiers Search"),
            Text.literal("Player not found in the PaleTiers API: " + playerName)
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        context.fillGradient(centerX - 160, centerY - 60, centerX + 160, centerY + 50, 0xCC000000, 0xCC000000);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Search Player"), centerX, centerY - 50, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);

        if (this.isSearching) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Searching..."), centerX, centerY + 35, 0xFFFFFF);
        } else if (this.errorMessage != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(this.errorMessage), centerX, centerY + 35, 0xFFFFFF);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Press ESC to close"), centerX, centerY + 35, 0x888888);
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
