package com.gimr12.towers_attack.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * A Button that draws the mod's own dark/gold panel style instead of
 * vanilla's gray 9-slice texture, so it actually matches the rest of the
 * Status Menu / Title screen instead of looking like a leftover vanilla
 * widget dropped onto a custom background.
 */
public class StyledButton extends Button {

    private final int fillColor;
    private final int hoverColor;
    private final int textColor;
    private final int borderOuter;
    private final int borderInner;

    protected StyledButton(int x, int y, int width, int height, Component message, OnPress onPress,
                            int fillColor, int hoverColor, int textColor, int borderOuter, int borderInner) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.fillColor = fillColor;
        this.hoverColor = hoverColor;
        this.textColor = textColor;
        this.borderOuter = borderOuter;
        this.borderInner = borderInner;
    }

    /** The standard look - dark violet body, gold border, gold text. Use for most buttons. */
    public static StyledButton primary(int x, int y, int width, int height, Component message, OnPress onPress) {
        return new StyledButton(x, y, width, height, message, onPress,
                0xFF1B1830, 0xFF2E2750, 0xFFE9C878, 0xFF7A5C2E, 0xFFD8B463);
    }

    /** A quieter look for less prominent actions (e.g. a plain "Exit"). */
    public static StyledButton neutral(int x, int y, int width, int height, Component message, OnPress onPress) {
        return new StyledButton(x, y, width, height, message, onPress,
                0xFF17151F, 0xFF262233, 0xFFB8B4C8, 0xFF3C3F52, 0xFF6E7390);
    }

    /** A warm red look for destructive/close actions. */
    public static StyledButton danger(int x, int y, int width, int height, Component message, OnPress onPress) {
        return new StyledButton(x, y, width, height, message, onPress,
                0xFF2E1414, 0xFF4A1F1F, 0xFFE98787, 0xFF7A3A2E, 0xFFD88463);
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        boolean hovered = isHoveredOrFocused();
        int fill = !active ? 0xFF141220 : (hovered ? hoverColor : fillColor);
        int outer = active ? borderOuter : 0xFF33303F;
        int inner = active ? borderInner : 0xFF4A4758;

        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, outer);
        g.fill(x, y, x + w, y + h, inner);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);

        int color = active ? textColor : 0xFF6B6B7A;
        g.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font, getMessage(),
                x + w / 2, y + (h - 8) / 2, color);
    }
}
