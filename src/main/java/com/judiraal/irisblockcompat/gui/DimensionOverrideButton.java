package com.judiraal.irisblockcompat.gui;

import com.judiraal.irisblockcompat.IrisBlockCompatConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.gl.uniform.FloatSupplier;
import net.irisshaders.iris.gui.GuiUtil;
import net.irisshaders.iris.gui.element.screen.IrisButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class DimensionOverrideButton extends IrisButton {
    private static final Component TRUE_TEXT = Component.translatable("label.iris.true").withStyle(ChatFormatting.GREEN);
    private static final Component FALSE_TEXT = Component.translatable("label.iris.false");
    private final FloatSupplier alphaSupplier;

    private boolean state;

    public DimensionOverrideButton(int x, int y, int width, int height, FloatSupplier alpha) {
        super(x, y, width, height, Component.literal("Dimension Override"),
                DimensionOverrideButton::buttonPressed,
                Button.DEFAULT_NARRATION, alpha);
        this.state = IrisBlockCompatConfig.getDimensionSettings().isPresent();
        this.alphaSupplier = alpha;
    }

    private static void buttonPressed(Button button) {
        if (button instanceof DimensionOverrideButton dButton) dButton.buttonPressed();
    }

    private void buttonPressed() {
        state = !state;
    }

    public boolean useDimensionOverride() {
        return state;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, (this.isHoveredOrFocused() || state) ? this.alphaSupplier.getAsFloat() * 1.8F : this.alphaSupplier.getAsFloat());
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        GuiUtil.bindIrisWidgetsTexture();
        GuiUtil.drawButton(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.isHoveredOrFocused(), this.active);
        GuiUtil.drawButton(guiGraphics, this.getX() + this.getWidth() - 30, this.getY() + 2, 28, this.getHeight() - 4, false, true);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alphaSupplier.getAsFloat());
        int color = this.active ? 0xFFFFFF : 0xA0A0A0;
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, this.getMessage(), this.getX() + 6, this.getY() + 7, color);
        Component stateText = state ? TRUE_TEXT : FALSE_TEXT;
        int stateX = this.getX() + this.getWidth() - 15 - font.width(stateText) / 2;
        guiGraphics.drawString(font, stateText, stateX, this.getY() + 7, color);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
