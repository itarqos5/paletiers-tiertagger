package com.paletiers.tiertagger.client.gui;

import com.paletiers.tiertagger.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private int centerX;
    private int spacing = 25;
    private ConfigCategory currentCategory = ConfigCategory.GENERAL;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private enum ConfigCategory {
        GENERAL("General"),
        COLOURS("Colours");

        private final String displayName;

        ConfigCategory(String displayName) {
            this.displayName = displayName;
        }
    }

    public ConfigScreen(Screen parent) {
        super(Text.literal("PaleTiers Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        centerX = this.width / 2;
        scrollOffset = 0;

        int tabY = 45;
        int tabWidth = 100;
        int tabSpacing = 5;
        int startTabX = centerX - (ConfigCategory.values().length * (tabWidth + tabSpacing)) / 2;

        for (int i = 0; i < ConfigCategory.values().length; i++) {
            ConfigCategory category = ConfigCategory.values()[i];
            int tabX = startTabX + i * (tabWidth + tabSpacing);
            boolean isSelected = category == this.currentCategory;

            this.addDrawableChild(ButtonWidget.builder(
                Text.literal(isSelected ? "[ " + category.displayName + " ]" : category.displayName),
                button -> {
                    this.currentCategory = category;
                    this.clearAndInit();
                }
            ).dimensions(tabX, tabY, tabWidth, 20).build());
        }

        if (this.currentCategory == ConfigCategory.GENERAL) {
            initGeneralCategory();
        } else {
            initColoursCategory();
        }

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Done"),
            button -> {
                if (this.client != null) {
                    this.client.setScreen(parent);
                }
            }
        ).dimensions(centerX - 100, this.height - 30, 200, 20).build());
    }

    private void initGeneralCategory() {
        int startY = 80;

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Mod Status: " + (ModConfig.isEnabled() ? "Enabled" : "Disabled")),
            button -> {
                ModConfig.setEnabled(!ModConfig.isEnabled());
                button.setMessage(Text.literal("Mod Status: " + (ModConfig.isEnabled() ? "Enabled" : "Disabled")));
            }
        ).dimensions(centerX - 100, startY, 200, 20).tooltip(Tooltip.of(Text.literal("Toggle tier display on/off"))).build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Gamemode Icons: " + (ModConfig.shouldShowGamemode() ? "Shown" : "Hidden")),
            button -> {
                ModConfig.setShowGamemode(!ModConfig.shouldShowGamemode());
                button.setMessage(Text.literal("Gamemode Icons: " + (ModConfig.shouldShowGamemode() ? "Shown" : "Hidden")));
            }
        ).dimensions(centerX - 100, startY + spacing, 200, 20).tooltip(Tooltip.of(Text.literal("Toggle gamemode icons before tier display"))).build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Gamemode: " + ModConfig.getSelectedGamemode()),
            button -> {
                ModConfig.cycleGamemode();
                button.setMessage(Text.literal("Gamemode: " + ModConfig.getSelectedGamemode()));
            }
        ).dimensions(centerX - 100, startY + spacing * 2, 200, 20).tooltip(Tooltip.of(Text.literal("Press G in-game to cycle"))).build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(ModConfig.shouldShowHighestTier() ? "Show Highest Tier: ON" : "Show Highest Tier: OFF"),
            button -> {
                ModConfig.setShowHighestTier(!ModConfig.shouldShowHighestTier());
                button.setMessage(Text.literal(ModConfig.shouldShowHighestTier() ? "Show Highest Tier: ON" : "Show Highest Tier: OFF"));
            }
        ).dimensions(centerX - 100, startY + spacing * 3, 200, 20).tooltip(Tooltip.of(Text.literal("ON: highest tier. OFF: selected gamemode"))).build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(ModConfig.isDebugMode() ? "Disable Debug" : "Enable Debug"),
            button -> {
                ModConfig.setDebugMode(!ModConfig.isDebugMode());
                button.setMessage(Text.literal(ModConfig.isDebugMode() ? "Disable Debug" : "Enable Debug"));
            }
        ).dimensions(centerX - 100, startY + spacing * 4, 200, 20).tooltip(Tooltip.of(Text.literal("Enable debug logging"))).build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Search Player"),
            button -> {
                if (this.client != null) {
                    this.client.setScreen(new PlayerSearchScreen(this));
                }
            }
        ).dimensions(centerX - 100, startY + spacing * 5, 200, 20).tooltip(Tooltip.of(Text.literal("Search for player tier info"))).build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Reset All Settings"),
            button -> {
                ModConfig.resetToDefaults();
                this.clearAndInit();
            }
        ).dimensions(centerX - 100, startY + spacing * 6, 200, 20).tooltip(Tooltip.of(Text.literal("Reset all settings to defaults"))).build());
    }

    private void initColoursCategory() {
        int startY = 75;
        int rowHeight = 26;
        int row = 0;

        int availableHeight = this.height - 120;
        int totalContentHeight = rowHeight * 10;
        maxScroll = Math.max(0, totalContentHeight - availableHeight);

        addTierColorRow("HT1", centerX - 180, startY + rowHeight * row++ - scrollOffset, ModConfig.getColorHT1(), ModConfig::setColorHT1, ModConfig.getGradientColorHT1(), ModConfig::setGradientColorHT1, ModConfig.isGradientEnabledHT1(), ModConfig::setGradientEnabledHT1);
        addTierColorRow("LT1", centerX - 180, startY + rowHeight * row++ - scrollOffset, ModConfig.getColorLT1(), ModConfig::setColorLT1, ModConfig.getGradientColorLT1(), ModConfig::setGradientColorLT1, ModConfig.isGradientEnabledLT1(), ModConfig::setGradientEnabledLT1);
        addTierColorRow("HT2", centerX - 180, startY + rowHeight * row++ - scrollOffset, ModConfig.getColorHT2(), ModConfig::setColorHT2, ModConfig.getGradientColorHT2(), ModConfig::setGradientColorHT2, ModConfig.isGradientEnabledHT2(), ModConfig::setGradientEnabledHT2);
        addTierColorRow("LT2", centerX - 180, startY + rowHeight * row++ - scrollOffset, ModConfig.getColorLT2(), ModConfig::setColorLT2, ModConfig.getGradientColorLT2(), ModConfig::setGradientColorLT2, ModConfig.isGradientEnabledLT2(), ModConfig::setGradientEnabledLT2);
        addTierColorRow("HT3", centerX - 180, startY + rowHeight * row++ - scrollOffset, ModConfig.getColorHT3(), ModConfig::setColorHT3, ModConfig.getGradientColorHT3(), ModConfig::setGradientColorHT3, ModConfig.isGradientEnabledHT3(), ModConfig::setGradientEnabledHT3);
        addTierColorRow("LT3", centerX - 180, startY + rowHeight * row++ - scrollOffset, ModConfig.getColorLT3(), ModConfig::setColorLT3, ModConfig.getGradientColorLT3(), ModConfig::setGradientColorLT3, ModConfig.isGradientEnabledLT3(), ModConfig::setGradientEnabledLT3);
        addTierColorRow("HT4", centerX - 180, startY + rowHeight * row++ - scrollOffset, ModConfig.getColorHT4(), ModConfig::setColorHT4, ModConfig.getGradientColorHT4(), ModConfig::setGradientColorHT4, ModConfig.isGradientEnabledHT4(), ModConfig::setGradientEnabledHT4);
        addTierColorRow("LT4", centerX - 180, startY + rowHeight * row++ - scrollOffset, ModConfig.getColorLT4(), ModConfig::setColorLT4, ModConfig.getGradientColorLT4(), ModConfig::setGradientColorLT4, ModConfig.isGradientEnabledLT4(), ModConfig::setGradientEnabledLT4);
        addTierColorRow("HT5", centerX - 180, startY + rowHeight * row++ - scrollOffset, ModConfig.getColorHT5(), ModConfig::setColorHT5, ModConfig.getGradientColorHT5(), ModConfig::setGradientColorHT5, ModConfig.isGradientEnabledHT5(), ModConfig::setGradientEnabledHT5);
        addTierColorRow("LT5", centerX - 180, startY + rowHeight * row++ - scrollOffset, ModConfig.getColorLT5(), ModConfig::setColorLT5, ModConfig.getGradientColorLT5(), ModConfig::setGradientColorLT5, ModConfig.isGradientEnabledLT5(), ModConfig::setGradientEnabledLT5);

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Reset All Colors"),
            button -> {
                ModConfig.resetColorsToDefaults();
                this.clearAndInit();
            }
        ).dimensions(centerX - 90, this.height - 55, 180, 22).tooltip(Tooltip.of(Text.literal("Reset all color settings"))).build());
    }

    private void addTierColorRow(String tierName, int x, int y,
                                 int primaryColor, java.util.function.Consumer<Integer> onPrimaryChange,
                                 int gradientColor, java.util.function.Consumer<Integer> onGradientChange,
                                 boolean gradientEnabled, java.util.function.Consumer<Boolean> onGradientToggle) {
        int topBound = 70;
        int bottomBound = this.height - 60;
        boolean isVisible = y >= topBound && y + 25 <= bottomBound;
        if (!isVisible) {
            return;
        }

        this.addDrawable((context, mouseX, mouseY, delta) -> context.drawTextWithShadow(this.textRenderer, Text.literal(tierName), x, y + 5, Colors.WHITE));
        this.addDrawable((context, mouseX, mouseY, delta) -> context.drawTextWithShadow(this.textRenderer, Text.literal("Color:"), x + 40, y + 5, 0xAAAAAA));

        ColorPickerWidget primaryPicker = new ColorPickerWidget(x + 80, y + 2, 32, 20, primaryColor, onPrimaryChange);
        this.addDrawableChild(primaryPicker);

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(gradientEnabled ? "Grad ON" : "Grad OFF"),
            button -> {
                onGradientToggle.accept(!gradientEnabled);
                this.clearAndInit();
            }
        ).dimensions(x + 120, y + 2, 60, 20).build());

        if (gradientEnabled) {
            this.addDrawable((context, mouseX, mouseY, delta) -> context.drawTextWithShadow(this.textRenderer, Text.literal("->"), x + 186, y + 5, 0x888888));
            ColorPickerWidget gradientPicker = new ColorPickerWidget(x + 204, y + 2, 32, 20, gradientColor, onGradientChange);
            this.addDrawableChild(gradientPicker);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, 20, Colors.WHITE);

        if (this.currentCategory == ConfigCategory.GENERAL) {
            int infoY = this.height - 70;
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Cache Time: " + ModConfig.getCacheTime() / 60000 + " minutes"), centerX, infoY, Colors.WHITE);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Keybind: Press G to cycle gamemode"), centerX, infoY + 12, Colors.WHITE);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Customize tier colors and gradients"), centerX, this.height - 80, 0xAAAAAA);
            if (maxScroll > 0) {
                if (scrollOffset > 0) {
                    context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Scroll Up"), centerX, 68, 0xFFFFFF);
                }
                if (scrollOffset < maxScroll) {
                    context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Scroll Down"), centerX, this.height - 95, 0xFFFFFF);
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.currentCategory == ConfigCategory.COLOURS && maxScroll > 0) {
            int scrollAmount = (int) (verticalAmount * 20);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scrollAmount));
            this.clearAndInit();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
