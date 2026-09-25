package com.gimr12.towers_attack.client.gui;

import com.gimr12.towers_attack.client.ClientTitleData;
import com.gimr12.towers_attack.client.ClientPlayerData;
import com.gimr12.towers_attack.leveling.PlayerLeveling;
import com.gimr12.towers_attack.leveling.StatType;
import com.gimr12.towers_attack.network.EquipTitlePacket;
import com.gimr12.towers_attack.titles.TitleDefinition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Parchment-book style title browser: numbered paged list on the left, details on the right. */
public class TitleScreen extends Screen {
    private static final int W = 390;
    private static final int H = 222;
    private static final int ROWS_PER_PAGE = 4;
    private static final int ROW_H = 32;
    private static final int LIST_W = 200;

    // Parchment palette
    private static final int PANEL_BG = 0xFFE9CDA3;
    private static final int PANEL_BORDER = 0xFF4A3319;
    private static final int ROW_BG = 0xFFDCBC8B;
    private static final int ROW_BG_SELECTED = 0xFFF1DEB3;
    private static final int TEXT_DARK = 0xFF3A2410;
    private static final int TEXT_MUTED = 0xFF6B5030;

    private int page = 0;
    private int selected = 0;
    private StyledButton equipButton;
    private Button prevPageButton;
    private Button nextPageButton;
    private int previousMenuBlur = 0;

    public TitleScreen() { super(Component.literal("Titles")); }

    @Override
    protected void init() {
        super.init();

        if (this.minecraft != null) {
            previousMenuBlur = this.minecraft.options.menuBackgroundBlurriness().get();
            this.minecraft.options.menuBackgroundBlurriness().set(0);
        }

        int x = (width - W) / 2;
        int y = (height - H) / 2;

        equipButton = StyledButton.danger(x + LIST_W + 40, y + H - 40, W - LIST_W - 60, 24,
                Component.literal("Equip"), b -> equipSelected());
        addRenderableWidget(equipButton);

        int pageBtnY = y + H - 26;
        prevPageButton = StyledButton.neutral(x + 14, pageBtnY, 20, 18, Component.literal("<"), b -> {
            if (page > 0) page--;
        });
        nextPageButton = StyledButton.neutral(x + LIST_W - 18, pageBtnY, 20, 18, Component.literal(">"), b -> {
            int maxPage = Math.max(0, (ClientTitleData.getTitles().size() - 1) / ROWS_PER_PAGE);
            if (page < maxPage) page++;
        });
        addRenderableWidget(prevPageButton);
        addRenderableWidget(nextPageButton);

        addRenderableWidget(StyledButton.neutral(x + W - 60, y + 10, 46, 16,
                Component.literal("Exit"), b -> this.onClose()));

        updateEquipButton();
    }

    @Override
    public void removed() {
        super.removed();
        if (this.minecraft != null) {
            this.minecraft.options.menuBackgroundBlurriness().set(previousMenuBlur);
        }
    }

    // ----- row layout, shared between drawing and click handling -----

    private record RowRect(int index, TitleDefinition title, int x, int y, int w, int h) {}

    private List<RowRect> currentRows() {
        List<RowRect> rows = new ArrayList<>();
        List<TitleDefinition> titles = ClientTitleData.getTitles();
        if (titles.isEmpty()) return rows;

        int maxPage = Math.max(0, (titles.size() - 1) / ROWS_PER_PAGE);
        page = Math.max(0, Math.min(page, maxPage));

        int x = (width - W) / 2 + 14;
        int y = (height - H) / 2 + 44;
        int rowW = LIST_W - 28;
        int rowHeight = ROW_H - 2;

        int start = page * ROWS_PER_PAGE;
        for (int i = 0; i < ROWS_PER_PAGE && start + i < titles.size(); i++) {
            int index = start + i;
            rows.add(new RowRect(index, titles.get(index), x, y + i * ROW_H, rowW, rowHeight));
        }
        return rows;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xC0000000);
        int x = (width - W) / 2;
        int y = (height - H) / 2;

        g.fill(x - 3, y - 3, x + W + 3, y + H + 3, PANEL_BORDER);
        g.fill(x, y, x + W, y + H, PANEL_BG);
        g.fill(x + LIST_W, y + 6, x + LIST_W + 2, y + H - 6, PANEL_BORDER);

        g.drawCenteredString(font, Component.literal("Titles"), x + LIST_W / 2, y + 12, TEXT_DARK);

        List<TitleDefinition> titles = ClientTitleData.getTitles();
        int maxPage = Math.max(0, (titles.size() - 1) / ROWS_PER_PAGE);
        g.drawCenteredString(font, Component.literal("Page " + (page + 1) + "/" + (maxPage + 1)),
                x + LIST_W / 2, y + H - 24, TEXT_MUTED);
        prevPageButton.active = page > 0;
        nextPageButton.active = page < maxPage;

        for (RowRect row : currentRows()) {
            drawRow(g, row);
        }

        if (!titles.isEmpty()) {
            TitleDefinition title = titles.get(Math.max(0, Math.min(selected, titles.size() - 1)));
            drawDetails(g, x + LIST_W + 20, y + 14, x + W - 20, title);
        } else {
            g.drawString(font, Component.literal("Waiting for title data from the server..."), x + LIST_W + 20, y + 30, TEXT_DARK, false);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawRow(GuiGraphics g, RowRect row) {
        // TEMP DIAGNOSTIC: an unmissable bright red block. If this doesn't show up in
        // your next screenshot, drawRow() itself isn't running (or its output is being
        // hidden/clipped by something else) - a completely different bug than a text
        // color/data issue. If it DOES show up, the method runs fine and the problem
        // is specifically in the text/color calls below it.
        g.fill(row.x(), row.y(), row.x() + 50, row.y() + 20, 0xFFFF0000);

        TitleDefinition title = row.title();
        boolean isSelected = row.index() == selected;
        boolean unlocked = ClientTitleData.isUnlocked(title.id());

        int bg = isSelected ? ROW_BG_SELECTED : ROW_BG;
        g.fill(row.x(), row.y(), row.x() + row.w(), row.y() + row.h(), bg);

        int stripeColor = title.rarity().color() | 0xFF000000;
        g.fill(row.x(), row.y(), row.x() + 4, row.y() + row.h(), stripeColor);

        g.drawString(font, Component.literal("#" + (row.index() + 1)), row.x() + 10, row.y() + 5, TEXT_MUTED, false);

        String label = unlocked ? title.name() : "???";
        int textColor = unlocked ? (title.rarity().color() | 0xFF000000) : TEXT_MUTED;
        g.drawString(font, Component.literal(label), row.x() + 34, row.y() + 5, textColor, false);

        if (!unlocked) {
            g.drawString(font, Component.literal("Locked"), row.x() + 34, row.y() + 17, TEXT_MUTED, false);
        }

        if (isSelected) {
            g.fill(row.x(), row.y() + row.h() - 1, row.x() + row.w(), row.y() + row.h(), PANEL_BORDER);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (RowRect row : currentRows()) {
            if (mouseX >= row.x() && mouseX < row.x() + row.w() && mouseY >= row.y() && mouseY < row.y() + row.h()) {
                selected = row.index();
                updateEquipButton();
                return true;
            }
        }
        return false;
    }

    private void drawDetails(GuiGraphics g, int x, int y, int maxX, TitleDefinition title) {
        boolean unlocked = ClientTitleData.isUnlocked(title.id());
        int color = title.rarity().color() | 0xFF000000;

        g.drawString(font, Component.literal(title.name()), x, y, color, false);
        String rarityLabel = (title.rarity().ordinal() >= com.gimr12.towers_attack.titles.TitleRarity.MYTHIC.ordinal() ? "\u2605 " : "")
                + capitalize(title.rarity().name().replace('_', ' '));
        g.drawString(font, Component.literal(rarityLabel), x, y + 12, title.rarity().color(), false);

        int line = y + 30;
        g.drawString(font, Component.literal("Description:"), x, line, TEXT_DARK, false);
        line += 11;
        for (String wrapped : wrap(title.description().isBlank() ? "No description." : title.description(), maxX - x)) {
            g.drawString(font, Component.literal(wrapped), x, line, TEXT_MUTED, false);
            line += 10;
        }

        line += 8;
        g.drawString(font, Component.literal("Title Perks:"), x, line, TEXT_DARK, false);
        line += 11;
        if (title.stats().isEmpty()) {
            g.drawString(font, Component.literal("No stat bonuses."), x, line, 0xFF3F8F3F, false);
            line += 10;
        } else {
            for (Map.Entry<String, Double> e : title.stats().entrySet()) {
                StatType type = statTypeFor(e.getKey());
                String statLabel = type != null ? type.getDisplayName() : capitalize(e.getKey());
                g.drawString(font, Component.literal("Increases " + statLabel + " by " + trim(e.getValue())), x, line, 0xFF3F8F3F, false);
                line += 10;
            }
        }

        line += 8;
        g.drawString(font, Component.literal("Requirements:"), x, line, TEXT_DARK, false);
        line += 11;
        if (title.commandOnly()) {
            g.drawString(font, Component.literal("Command only - granted by /gimr titles grant"), x, line, 0xFFAA2222, false);
            line += 10;
        } else {
            int level = PlayerLeveling.levelForGp(ClientPlayerData.getGP());
            if (title.minimumPlayerLevel() > 1) {
                g.drawString(font, Component.literal("Player level: " + level + "/" + title.minimumPlayerLevel()), x, line, TEXT_MUTED, false);
                line += 10;
            }
            for (Map.Entry<String, Integer> e : title.killEntities().entrySet()) {
                int required = title.requiredKills(e.getValue(), level);
                drawProgress(g, x, line, pretty(e.getKey()), ClientTitleData.getKillCount(e.getKey()), required);
                line += 10;
            }
            for (Map.Entry<String, Integer> e : title.killTags().entrySet()) {
                int required = title.requiredKills(e.getValue(), level);
                drawProgress(g, x, line, pretty(e.getKey()) + " (tag)", ClientTitleData.getKillCount("#" + e.getKey()), required);
                line += 10;
            }
            for (Map.Entry<String, Integer> e : title.mineBlocks().entrySet()) {
                int have = ClientTitleData.getKillCount("stat:mine_block:" + e.getKey());
                int[] scaled = scaleForDisplay(e.getKey(), have, title.requiredKills(e.getValue(), level));
                drawProgress(g, x, line, "Mine " + pretty(e.getKey()), scaled[0], scaled[1]);
                line += 10;
            }
            for (Map.Entry<String, Integer> e : title.craftItems().entrySet()) {
                int have = ClientTitleData.getKillCount("stat:craft_item:" + e.getKey());
                int[] scaled = scaleForDisplay(e.getKey(), have, title.requiredKills(e.getValue(), level));
                drawProgress(g, x, line, "Craft " + pretty(e.getKey()), scaled[0], scaled[1]);
                line += 10;
            }
            for (Map.Entry<String, Integer> e : title.customStats().entrySet()) {
                int have = ClientTitleData.getKillCount("stat:custom_stat:" + e.getKey());
                int[] scaled = scaleForDisplay(e.getKey(), have, title.requiredKills(e.getValue(), level));
                drawProgress(g, x, line, prettyCustomStat(e.getKey()), scaled[0], scaled[1]);
                line += 10;
            }
        }

        line += 10;
        g.fill(x, line, maxX, line + 1, PANEL_BORDER);
        line += 8;
        g.drawString(font, Component.literal("Currently Equipped:"), x, line, TEXT_DARK, false);
        line += 11;
        if (minecraft != null && minecraft.player != null) {
            TitleDefinition equipped = ClientTitleData.getEquippedTitle();
            String equippedName = equipped != null ? equipped.name() : "None";
            int equippedColor = equipped != null ? (equipped.rarity().color() | 0xFF000000) : TEXT_MUTED;
            g.drawString(font, Component.literal(minecraft.player.getGameProfile().getName()), x, line, TEXT_DARK, false);
            g.drawString(font, Component.literal(equippedName), x + 100, line, equippedColor, false);
        }
    }

    private void drawProgress(GuiGraphics g, int x, int y, String name, int have, int required) {
        String mark = have >= required ? "\u2713 " : "";
        int color = have >= required ? 0xFF3F8F3F : TEXT_MUTED;
        g.drawString(font, Component.literal(mark + name + ": " + Math.min(have, required) + "/" + required), x, y, color, false);
    }

    private StatType statTypeFor(String key) {
        for (StatType t : StatType.values()) {
            if (t.name().equalsIgnoreCase(key)) return t;
        }
        return null;
    }

    private List<String> wrap(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (font.width(candidate) > maxWidth && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines;
    }

    private void equipSelected() {
        List<TitleDefinition> titles = ClientTitleData.getTitles();
        if (titles.isEmpty() || minecraft == null) return;
        TitleDefinition title = titles.get(Math.max(0, Math.min(selected, titles.size() - 1)));
        if (!ClientTitleData.isUnlocked(title.id())) return;
        if (ClientTitleData.getEquipped().equals(title.id()))
            PacketDistributor.sendToServer(new EquipTitlePacket(""));
        else
            PacketDistributor.sendToServer(new EquipTitlePacket(title.id()));
    }

    private void updateEquipButton() {
        if (equipButton == null) return;
        List<TitleDefinition> titles = ClientTitleData.getTitles();
        if (titles.isEmpty()) { equipButton.active = false; return; }
        TitleDefinition title = titles.get(Math.max(0, Math.min(selected, titles.size() - 1)));
        boolean unlocked = ClientTitleData.isUnlocked(title.id());
        equipButton.active = unlocked;
        equipButton.setMessage(Component.literal(ClientTitleData.getEquipped().equals(title.id()) ? "Remove Title" : "Equip Title"));
    }

    private int[] scaleForDisplay(String statId, int have, int required) {
        if (statId.equals("minecraft:play_time") || statId.equals("minecraft:total_world_time")) {
            return new int[]{have / 72000, Math.max(1, required / 72000)};
        }
        if (statId.endsWith("_one_cm")) {
            return new int[]{have / 100, Math.max(1, required / 100)};
        }
        return new int[]{have, required};
    }

    private String prettyCustomStat(String id) {
        String clean = pretty(id);
        if (id.equals("minecraft:play_time") || id.equals("minecraft:total_world_time")) return "Play time (hrs)";
        if (id.endsWith("_one_cm")) return clean.replace(" one cm", "") + " (blocks)";
        return clean;
    }

    private String pretty(String s) {
        String clean = s.startsWith("#") ? s.substring(1) : s;
        int colon = clean.indexOf(':');
        if (colon >= 0) clean = clean.substring(colon + 1);
        return clean.replace('_', ' ');
    }

    private String capitalize(String s) {
        if (s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private String trim(double d) {
        if (d == (long) d) return Long.toString((long) d);
        return String.format(java.util.Locale.ROOT, "%.2f", d);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        List<TitleDefinition> titles = ClientTitleData.getTitles();
        int maxPage = Math.max(0, (titles.size() - 1) / ROWS_PER_PAGE);
        if (deltaY < 0 && page < maxPage) page++;
        else if (deltaY > 0 && page > 0) page--;
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (equipButton != null && minecraft != null && minecraft.player != null) {
            updateEquipButton();
        }
    }

    @Override public boolean isPauseScreen() { return false; }
}
