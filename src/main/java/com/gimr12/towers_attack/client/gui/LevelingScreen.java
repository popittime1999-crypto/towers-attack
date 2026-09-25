package com.gimr12.towers_attack.client.gui;

import com.gimr12.towers_attack.client.ClientPlayerData;
import com.gimr12.towers_attack.leveling.PlayerLeveling;
import com.gimr12.towers_attack.leveling.StatType;
import com.gimr12.towers_attack.network.AllocateStatPacket;
import com.gimr12.towers_attack.network.ResetStatsPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

import static com.gimr12.towers_attack.attachment.ModAttachments.*;
import static com.gimr12.towers_attack.client.gui.StatIcons.HP;

/**
 * "Status Menu" screen: your wireframe's layout (name/GP bar top, skin +
 * level left, stat grid + SP + Reset right), re-skinned with a dark/gold
 * Tensura-style palette, and 32x32 pixel-art stat icons.
 *
 * All art is drawn procedurally with GuiGraphics (no texture files needed).
 */
public class LevelingScreen extends Screen {

    // Palette - dark navy/violet body with gold accents
    private static final int COLOR_DIM_BG = 0xC0000000;
    private static final int COLOR_PANEL_BORDER_OUTER = 0xFF7A5C2E; // dark gold
    private static final int COLOR_PANEL_BORDER_INNER = 0xFFD8B463; // bright gold
    private static final int COLOR_PANEL_BG = 0xFF120F1C;           // near-black navy
    private static final int COLOR_BOX_BORDER = 0xFF6E4FE0;         // violet accent border
    private static final int COLOR_BOX_BG = 0xFF1B1830;             // dark box fill
    private static final int COLOR_TEXT = 0xFFE9E4F5;               // pale lavender
    private static final int COLOR_TEXT_GOLD = 0xFFE9C878;
    private static final int COLOR_TEXT_DIM = 0xFF8A83A8;
    private static final int COLOR_BAR_BG = 0xFF0A0816;
    private static final int COLOR_BAR_FILL_A = 0xFF3A2E7C;         // deep violet
    private static final int COLOR_BAR_FILL_B = 0xFF6E4FE0;         // bright violet
    private static final int COLOR_ICON_CAN_AFFORD = 0xFFE9C878;    // gold
    private static final int COLOR_ICON_CANT_AFFORD = 0xFF5A5670;   // dim gray-violet
    private static final int COLOR_ICON_HOVER_BG = 0xFF2E2750;

    // Smaller overall footprint than before, even with bigger icons
    private static final int PANEL_WIDTH = 390;
    private static final int PANEL_HEIGHT = 222;
    private static final int PAD = 10;
    private static final int GAP = 8;
    private static final int TOP_ROW_HEIGHT = 22;
    private static final int LEFT_COL_WIDTH = 120;
    private static final int RIGHT_COL_WIDTH = PANEL_WIDTH - PAD * 2 - LEFT_COL_WIDTH - GAP;
    private static final int CONTENT_HEIGHT = PANEL_HEIGHT - PAD * 2 - TOP_ROW_HEIGHT - GAP;

    private static final int ICON_SIZE = 32; // stat buttons are 32x32
    private static final int STAT_ROW_HEIGHT = 38;

    private int previousMenuBlur = 0;
    private Button resetButton;

    public LevelingScreen() {
        super(Component.literal("Status Menu"));
    }

    @Override
    protected void init() {
        super.init();

        if (this.minecraft != null) {
            previousMenuBlur = this.minecraft.options.menuBackgroundBlurriness().get();
            this.minecraft.options.menuBackgroundBlurriness().set(0);
        }

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        // Sits outside the panel's own border, above the top edge - keeps it
        // visually separate from the Status Menu's contents.
        int titleWidth = 64;
        int headerButtonHeight = 18;
        this.addRenderableWidget(StyledButton.primary(panelX, panelY - headerButtonHeight - 6, titleWidth, headerButtonHeight,
                Component.literal("Titles"), b -> {
                    if (this.minecraft != null) this.minecraft.setScreen(new TitleScreen());
                }));

        int closeSize = 18;
        this.addRenderableWidget(StyledButton.danger(panelX + PANEL_WIDTH - closeSize - 4, panelY - headerButtonHeight - 6, closeSize, closeSize,
                Component.literal("X"), b -> this.onClose()));

        Rect statsBox = statsBoxRect(panelX, panelY);
        int resetWidth = 56;
        int resetHeight = 18;
        int bottomRowY = statsBox.y + PAD + STAT_ROW_HEIGHT * 3 + 4;
        this.resetButton = StyledButton.danger(statsBox.x + statsBox.w - PAD - resetWidth, bottomRowY, resetWidth, resetHeight,
                Component.literal("Reset"), b -> {
                    if (this.minecraft != null) {
                        PacketDistributor.sendToServer(ResetStatsPacket.INSTANCE);
                    }
                });
        this.addRenderableWidget(this.resetButton);
    }

    @Override
    public void removed() {
        super.removed();
        if (this.minecraft != null) {
            this.minecraft.options.menuBackgroundBlurriness().set(previousMenuBlur);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, COLOR_DIM_BG);

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        drawPanelFrame(graphics, panelX, panelY);
        drawTopRow(graphics, panelX, panelY);
        drawLeftColumn(graphics, panelX, panelY, mouseX, mouseY);
        drawStatsPanel(graphics, panelX, panelY, mouseX, mouseY);

        if (this.resetButton != null) {
            this.resetButton.active = ClientPlayerData.getResetPoints() > 0;
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    // ----- layout helpers -----

    private record Rect(int x, int y, int w, int h) {}

    private Rect statsBoxRect(int panelX, int panelY) {
        int x = panelX + PAD + LEFT_COL_WIDTH + GAP;
        int y = panelY + PAD + TOP_ROW_HEIGHT + GAP;
        return new Rect(x, y, RIGHT_COL_WIDTH, CONTENT_HEIGHT);
    }

    private record StatSlot(StatType type, int x, int y) {}

    private List<StatSlot> statSlots(int panelX, int panelY) {
        Rect box = statsBoxRect(panelX, panelY);
        int col1X = box.x + PAD;
        int col2X = box.x + box.w / 2 + 2;
        int gridY = box.y + PAD;

        List<StatSlot> slots = new ArrayList<>();
        slots.add(new StatSlot(StatType.STRENGTH, col1X, gridY));
        slots.add(new StatSlot(StatType.HP, col2X, gridY));
        slots.add(new StatSlot(StatType.STAMINA, col1X, gridY + STAT_ROW_HEIGHT));
        slots.add(new StatSlot(StatType.DEF, col2X, gridY + STAT_ROW_HEIGHT));
        slots.add(new StatSlot(StatType.LUCK, col1X, gridY + STAT_ROW_HEIGHT * 2));
        return slots;
    }

    // ----- drawing -----

    private void drawPanelFrame(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 2, y - 2, x + PANEL_WIDTH + 2, y + PANEL_HEIGHT + 2, COLOR_PANEL_BORDER_OUTER);
        graphics.fill(x - 1, y - 1, x + PANEL_WIDTH + 1, y + PANEL_HEIGHT + 1, COLOR_PANEL_BORDER_INNER);
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, COLOR_PANEL_BG);
    }

    private void drawBox(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, COLOR_BOX_BORDER);
        graphics.fill(x, y, x + w, y + h, COLOR_BOX_BG);
    }

    private void drawTopRow(GuiGraphics graphics, int panelX, int panelY) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        Player player = this.minecraft.player;

        int nameX = panelX + PAD;
        int nameY = panelY + PAD;
        drawBox(graphics, nameX, nameY, LEFT_COL_WIDTH, TOP_ROW_HEIGHT);
        String name = player.getGameProfile().getName();
        graphics.drawCenteredString(this.font, Component.literal(name), nameX + LEFT_COL_WIDTH / 2, nameY + 7, COLOR_TEXT_GOLD);

        int gp = ClientPlayerData.getGP();
        int level = PlayerLeveling.levelForGp(gp);
        int gpIntoLevel = PlayerLeveling.getGpIntoCurrentLevel(gp, level);
        int gpForThisLevel = PlayerLeveling.getGpRequiredForCurrentLevel(level);
        float progress = gpForThisLevel <= 0 ? 0f : Mth.clamp((float) gpIntoLevel / gpForThisLevel, 0f, 1f);

        int barX = nameX + LEFT_COL_WIDTH + GAP;
        int barY = nameY;
        int barW = RIGHT_COL_WIDTH;
        int barH = TOP_ROW_HEIGHT;
        int fractionWidth = 50;
        int fillBarW = barW - fractionWidth - 6;

        drawBox(graphics, barX, barY, fillBarW, barH);
        int filled = Math.round((fillBarW - 4) * progress);
        if (filled > 0) {
            graphics.fill(barX + 2, barY + 2, barX + 2 + filled, barY + barH - 2, COLOR_BAR_FILL_A);
            graphics.fill(barX + 2, barY + 2, barX + 2 + filled, barY + Math.max(3, barH / 3), COLOR_BAR_FILL_B);
        }

        graphics.drawString(this.font, Component.literal(gpIntoLevel + "/" + gpForThisLevel),
                barX + fillBarW + 8, barY + 7, COLOR_TEXT, false);
    }

    private void drawLeftColumn(GuiGraphics graphics, int panelX, int panelY, int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        Player player = this.minecraft.player;

        int colX = panelX + PAD;
        int colY = panelY + PAD + TOP_ROW_HEIGHT + GAP;

        int skinHeight = CONTENT_HEIGHT - GAP - TOP_ROW_HEIGHT;
        drawBox(graphics, colX, colY, LEFT_COL_WIDTH, skinHeight);

        int x1 = colX + 16;
        int y1 = colY + 4;
        int x2 = colX + LEFT_COL_WIDTH - 16;
        int y2 = colY + skinHeight - 4;
        int scale = 26;
        float lookX = (float) (x1 + x2) / 2f - mouseX;
        float lookY = (float) (y1 + y2) / 2f - 30f - mouseY;
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x1, y1, x2, y2, scale, 0.0625F, lookX, lookY, player);

        int lvlY = colY + skinHeight + GAP;
        drawBox(graphics, colX, lvlY, LEFT_COL_WIDTH, TOP_ROW_HEIGHT);
        int level = PlayerLeveling.levelForGp(ClientPlayerData.getGP());
        graphics.drawCenteredString(this.font, Component.literal("lvl " + level), colX + LEFT_COL_WIDTH / 2, lvlY + 7, COLOR_TEXT_GOLD);
    }

    private void drawStatsPanel(GuiGraphics graphics, int panelX, int panelY, int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        Rect box = statsBoxRect(panelX, panelY);
        drawBox(graphics, box.x, box.y, box.w, box.h);

        int sp = ClientPlayerData.getSP();
        boolean canAfford = sp > 0;

        for (StatSlot slot : statSlots(panelX, panelY)) {
            int value = ClientPlayerData.getStat(slot.type);
            boolean hovered = isInside(mouseX, mouseY, slot.x, slot.y, ICON_SIZE, ICON_SIZE);

            if (hovered && canAfford) {
                graphics.fill(slot.x - 2, slot.y - 2, slot.x + ICON_SIZE + 2, slot.y + ICON_SIZE + 2, COLOR_ICON_HOVER_BG);
            }

            graphics.fill(slot.x, slot.y, slot.x + ICON_SIZE, slot.y + ICON_SIZE, COLOR_BOX_BG);
            int iconColor = canAfford ? COLOR_ICON_CAN_AFFORD : COLOR_ICON_CANT_AFFORD;
            drawSquareOutline(graphics, slot.x, slot.y, ICON_SIZE, iconColor);

            int[][] pattern = patternFor(slot.type);
            int pixelSize = 3; // 8 cells * 3px = 24px glyph, centered in the 32px button
            int iconX = slot.x + (ICON_SIZE - 8 * pixelSize) / 2;
            int iconY = slot.y + (ICON_SIZE - 8 * pixelSize) / 2;
            PixelIcon.draw(graphics, iconX, iconY, pixelSize, pattern, iconColor);

            String label = slot.type.getDisplayName() + " " + value;
            graphics.drawString(this.font, Component.literal(label), slot.x + ICON_SIZE + 5, slot.y + ICON_SIZE / 2 - 4, COLOR_TEXT, false);

            com.gimr12.towers_attack.titles.TitleDefinition equipped = com.gimr12.towers_attack.client.ClientTitleData.getEquippedTitle();
            if (equipped != null) {
                Double bonus = equipped.stats().get(slot.type.name().toLowerCase());
                if (bonus != null && bonus != 0) {
                    int labelWidth = this.font.width(label);
                    String bonusText = " +" + (bonus == Math.floor(bonus) ? String.valueOf(bonus.intValue()) : bonus.toString());
                    graphics.drawString(this.font, Component.literal(bonusText),
                            slot.x + ICON_SIZE + 5 + labelWidth, slot.y + ICON_SIZE / 2 - 4, 0xFF55FF55, false);
                }
            }
        }

        int bottomRowY = box.y + PAD + STAT_ROW_HEIGHT * 3 + 4;
        int resetPoints = ClientPlayerData.getResetPoints();
        graphics.drawString(this.font, Component.literal("SP: " + sp), box.x + PAD, bottomRowY + 2, COLOR_TEXT_GOLD, false);
        graphics.drawString(this.font, Component.literal("Reset Pts: " + resetPoints), box.x + PAD, bottomRowY + 12, COLOR_TEXT_DIM, false);
    }

    /** Rounded-square outline, generalized so it scales cleanly to any icon size. */
    private void drawSquareOutline(GuiGraphics graphics, int x, int y, int size, int color) {
        int steps = Math.max(2, size / 8);
        graphics.fill(x + steps, y, x + size - steps, y + 1, color);
        graphics.fill(x + steps, y + size - 1, x + size - steps, y + size, color);
        graphics.fill(x, y + steps, x + 1, y + size - steps, color);
        graphics.fill(x + size - 1, y + steps, x + size, y + size - steps, color);
        for (int i = 0; i < steps; i++) {
            int trim = steps - i;
            graphics.fill(x + trim - 1, y + i, x + trim, y + i + 1, color);
            graphics.fill(x + size - trim, y + i, x + size - trim + 1, y + i + 1, color);
            graphics.fill(x + trim - 1, y + size - i - 1, x + trim, y + size - i, color);
            graphics.fill(x + size - trim, y + size - i - 1, x + size - trim + 1, y + size - i, color);
        }
    }

    private int[][] patternFor(StatType type) {
        return switch (type) {
            case STRENGTH -> StatIcons.STRENGTH;
            case HP -> StatIcons.HP;
            case STAMINA -> StatIcons.STAMINA;
            case DEF -> StatIcons.DEF;
            case LUCK -> StatIcons.LUCK;
        };
    }

    private boolean isInside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    // ----- input -----

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.minecraft == null || this.minecraft.player == null) return false;

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        int sp = ClientPlayerData.getSP();
        if (sp <= 0) return false;

        for (StatSlot slot : statSlots(panelX, panelY)) {
            if (isInside((int) mouseX, (int) mouseY, slot.x, slot.y, ICON_SIZE, ICON_SIZE)) {
                PacketDistributor.sendToServer(new AllocateStatPacket(slot.type.name()));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
