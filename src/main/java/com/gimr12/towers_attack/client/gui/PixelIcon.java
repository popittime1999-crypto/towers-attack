package com.gimr12.towers_attack.client.gui;

import net.minecraft.client.gui.GuiGraphics;

/** Draws tiny pixel-grid icons from a list of {col, row} cells. No texture files needed. */
public class PixelIcon {
    public static void draw(GuiGraphics graphics, int x, int y, int pixelSize, int[][] cells, int color) {
        for (int[] cell : cells) {
            int px = x + cell[0] * pixelSize;
            int py = y + cell[1] * pixelSize;
            graphics.fill(px, py, px + pixelSize, py + pixelSize, color);
        }
    }
}
