package cn.remix.ui.clickgui;

import cn.remix.config.ConfigManager;
import cn.remix.module.Category;
import cn.remix.module.Module;
import cn.remix.module.impl.render.ClickGui;
import cn.remix.module.impl.render.HUD;
import cn.remix.module.impl.render.Translator;
import cn.remix.ui.clickgui.component.Component;
import cn.remix.util.IMinecraft;
import cn.remix.util.animation.Easing;
import cn.remix.util.animation.EasingAnimation;
import cn.remix.util.misc.KeyUtil;
import cn.remix.util.render.ColorUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import dev.bsprout.brapi.client.BFont;
import dev.bsprout.brapi.client.BRender;
import dev.bsprout.brapi.client.BTexture;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class CelestialClickGuiScreen extends Screen implements IMinecraft {

    private static final float WINDOW_WIDTH = 620;
    private static final float WINDOW_HEIGHT = 400;
    private static final float SIDEBAR_WIDTH = 120;
    private static final float SEARCH_HEIGHT = 22;
    private static final float ROW_HEIGHT = 32;
    private static final float ROW_GAP = 3;
    private static final float PADDING = 12;
    private static final float CONFIG_ROW_HEIGHT = 22;
    private static final float CONFIG_BTN_WIDTH = 70;
    private static final float CAT_HEIGHT = 26;

    private Category selectedCategory = Category.Combat;
    private boolean configMode = false;

    private final List<ModuleRow> rows = new ArrayList<>();
    private final List<String> configNames = new ArrayList<>();
    private int selectedConfigIndex = -1;

    private float scrollOffset = 0;
    private float targetScroll = 0;

    private final EasingAnimation openAnimation = new EasingAnimation(Easing.EASE_OUT_CUBIC, 260);

    private BFont bFont;
    private BTexture logoTexture;

    private String searchQuery = "";
    private boolean searchFocused = false;

    public CelestialClickGuiScreen() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        openAnimation.reset();

        if (bFont == null) {
            try { bFont = new BFont(Identifier.of("brapi", "fonts/noto_sans_regular.ttf")); }
            catch (Exception e) { bFont = null; }
        }
        if (logoTexture == null) {
            try { logoTexture = new BTexture(Identifier.of("remix", "textures/gui/logo.png")); }
            catch (Exception e) { logoTexture = null; }
        }

        searchQuery = "";
        searchFocused = false;
        refreshConfigs();
        rebuildRows();
    }

    private void refreshConfigs() {
        configNames.clear();
        configNames.addAll(instance.getConfigManager().getAvailableConfigs());
        if (selectedConfigIndex >= configNames.size()) selectedConfigIndex = -1;
    }

    private void rebuildRows() {
        rows.clear();
        if (configMode) return;

                String query = searchQuery.toLowerCase().trim();
        for (Module m : instance.getModuleManager().getModuleMap().values()) {
            if (m.getCategory() != selectedCategory) continue;
            if (!query.isEmpty()) {
                String original = m.getName().toLowerCase();
                String translated = Translator.module(m.getName()).toLowerCase();
                String description = m.getDescription().toLowerCase();
                if (!original.contains(query) && !translated.contains(query) && !description.contains(query)) continue;
            }
            rows.add(new ModuleRow(m));
        }
        scrollOffset = 0;
        targetScroll = 0;
    }   // ← ЗАКРЫВАЮЩАЯ СКОБКА МЕТОДА rebuildRows

    /** Плавное фиолетовое переливание (как в MainMenu). */
    private static int purpleShift(float brightness, float offset) {
        float t = (float) ((Math.sin(System.currentTimeMillis() / 3000.0 + offset) + 1.0) / 2.0);
        int r = (int) (10 + 25 * t * brightness);
        int g = (int) (5 + 10 * t * brightness);
        int b = (int) (20 + 50 * t * brightness);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static String categoryIcon(Category c) {
        switch (c) {
            case Combat:  return "\u2694";
            case Exploits:return "\u2620";
            case Move:    return "\u27A4";
            case Player:  return "\u265F";
            case World:   return "\u25C8";
            case Misc:    return "\u2699";
            case Render:  return "\u2726";
            default:      return "";
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        BRender.flushAll();

        openAnimation.run(1.0f);
        float open = openAnimation.getValue().floatValue();

        BRender b = new BRender();
        b.rect(0, 0, width, height, (int) (150 * open) << 24 | 0x000000, 0);

        float scale = 0.92f + 0.08f * open;
        float cx = width / 2f, cy = height / 2f;
        float drawW = WINDOW_WIDTH * scale;
        float drawH = WINDOW_HEIGHT * scale;
        float drawX = cx - drawW / 2f;
        float drawY = cy - drawH / 2f;

        // тень
        for (int i = 10; i > 0; i--) {
            int shadowAlpha = (int) (14 * (1.0f - i / 10.0f) * open);
            if (shadowAlpha <= 0) continue;
            b.roundRect((int) (drawX - i), (int) (drawY - i), (int) (drawW + i * 2), (int) (drawH + i * 2),
                    (shadowAlpha << 24), 12 + i, 1);
        }

        // бордер (фиолетовый)
        int b1 = purpleShift(1.0f, 0f);
        for (int i = 0; i < 2; i++) {
            int alpha = i == 0 ? 110 : 50;
            b.roundRect((int) (drawX - i - 1), (int) (drawY - i - 1),
                    (int) (drawW + (i + 1) * 2), (int) (drawH + (i + 1) * 2),
                    (alpha << 24) | (b1 & 0x00FFFFFF), 12 + i + 1, 1);
        }

        // фон окна (фиолетовый)
        int f1 = purpleShift(0.3f, 0f);
        int f2 = purpleShift(0.15f, 3.0f);
        b.roundRect((int) drawX, (int) drawY, (int) drawW, (int) drawH,
                (int) (245 * open) << 24 | (f1 & 0x00FFFFFF), 12, 2);

        b.roundRect((int) (drawX + 1), (int) (drawY + 1), (int) (drawW - 2), 40,
                (int) (28 * open) << 24 | 0x00FFFFFF, 12, 3);

        b.rect((int) (drawX + 12), (int) (drawY + 1), (int) (drawW - 24), 1,
                (int) (60 * open) << 24 | 0x00FFFFFF, 4);

        // ─── САЙДБАР ───
        float sidebarX = drawX + PADDING;
        float sidebarY = drawY + PADDING;

        if (logoTexture != null) {
            b.drawTexture(logoTexture, sidebarX, sidebarY, 28, 28, 0xFFFFFFFF, true, 5);
        }
        if (bFont != null) {
            b.drawText(bFont, "Nexus", sidebarX + 36, sidebarY + 9, 13f, 0xFFFFFFFF, 5);
        }

        // категории
        float catStartY = sidebarY + 40;
        float catY = catStartY;
        var font = instance.getFontManager().getBoldFont(16);

        for (Category c : Category.values()) {
            float catW = SIDEBAR_WIDTH - PADDING * 2;
            boolean active = c == selectedCategory;
            boolean hovered = mouseX >= sidebarX && mouseX <= sidebarX + catW
                    && mouseY >= catY && mouseY <= catY + CAT_HEIGHT - 4;

            int catBg;
            if (active) catBg = purpleShift(0.8f, 0f);
            else if (hovered) catBg = new Color(50, 40, 70, 230).getRGB();
            else catBg = new Color(28, 24, 34, 200).getRGB();

            b.roundRect((int) sidebarX, (int) catY, (int) catW, (int) (CAT_HEIGHT - 4), catBg, 6, 5);

            if (bFont != null) {
                String label = Translator.category(c.getName());
                int textColor = active ? 0xFFFFFFFF : 0xFFAAAAAA;
                b.drawText(bFont, label, sidebarX + 10, catY + 7, 10f, textColor, 6);
            }

            catY += CAT_HEIGHT;
        }

        // Configs + Accounts
        float bottomY = drawY + drawH - PADDING - 56;

        boolean configHovered = mouseX >= sidebarX && mouseX <= sidebarX + SIDEBAR_WIDTH - PADDING * 2
                && mouseY >= bottomY && mouseY <= bottomY + 22;
        int configBg = configMode ? purpleShift(0.8f, 0f)
                : (configHovered ? new Color(50, 40, 70, 230).getRGB() : new Color(28, 24, 34, 200).getRGB());
        b.roundRect((int) sidebarX, (int) bottomY, (int) (SIDEBAR_WIDTH - PADDING * 2), 22, configBg, 6, 5);
        if (bFont != null) {
            b.drawText(bFont, "Configs", sidebarX + 10, bottomY + 6, 10f,
                    configMode ? 0xFFFFFFFF : 0xFFAAAAAA, 6);
        }

        boolean accountsHoveredLocal = mouseX >= sidebarX && mouseX <= sidebarX + SIDEBAR_WIDTH - PADDING * 2
                && mouseY >= bottomY + 26 && mouseY <= bottomY + 48;
        int accBg = accountsHoveredLocal ? new Color(50, 40, 70, 230).getRGB() : new Color(28, 24, 34, 200).getRGB();
        b.roundRect((int) sidebarX, (int) bottomY + 26, (int) (SIDEBAR_WIDTH - PADDING * 2), 22, accBg, 6, 5);
        if (bFont != null) {
            b.drawText(bFont, "Accounts", sidebarX + 10, bottomY + 32, 10f, 0xFFAAAAAA, 6);
        }

        // ─── ОБЛАСТЬ СПИСКА ───
        float listX = drawX + SIDEBAR_WIDTH + PADDING;
        float listY = drawY + PADDING;
        float listW = drawW - SIDEBAR_WIDTH - PADDING * 2;
        float listH = drawH - PADDING * 2;

        if (configMode) {
            for (int i = 0; i < configNames.size(); i++) {
                String name = configNames.get(i);
                float y = listY + i * (CONFIG_ROW_HEIGHT + ROW_GAP);
                if (y + CONFIG_ROW_HEIGHT > listY + listH - 40) break;

                boolean selected = i == selectedConfigIndex;
                boolean hov = mouseX >= listX && mouseX <= listX + listW
                        && mouseY >= y && mouseY <= y + CONFIG_ROW_HEIGHT;

                int bg = selected ? purpleShift(0.8f, 0f)
                        : (hov ? new Color(50, 40, 70, 230).getRGB() : new Color(30, 26, 36, 210).getRGB());
                b.roundRect((int) listX, (int) y, (int) listW, (int) CONFIG_ROW_HEIGHT, bg, 5, 5);

                if (bFont != null) {
                    b.drawText(bFont, name, listX + 10, y + 7, 10f, 0xFFFFFFFF, 6);
                }
            }

            float btnY = listY + listH - 36;
            float btnW = (listW - 18) / 4f;
            String[] labels = {"Save", "Load", "Create", "Delete"};
            for (int i = 0; i < 4; i++) {
                float bx = listX + i * (btnW + 6);
                boolean hov = mouseX >= bx && mouseX <= bx + btnW && mouseY >= btnY && mouseY <= btnY + 24;
                int bg = hov ? new Color(60, 50, 80, 240).getRGB() : new Color(35, 30, 42, 220).getRGB();
                b.roundRect((int) bx, (int) btnY, (int) btnW, 24, bg, 5, 5);
                if (bFont != null) {
                    float tw = font.getStringWidth(labels[i]);
                    b.drawText(bFont, labels[i], bx + (btnW - tw) / 2f, btnY + 7, 10f, 0xFFFFFFFF, 6);
                }
            }
        } else {
            // поиск
            int searchBg = searchFocused ? new Color(45, 35, 60, 245).getRGB() : new Color(28, 24, 34, 220).getRGB();
            b.roundRect((int) listX, (int) listY, (int) listW, (int) SEARCH_HEIGHT, searchBg, 6, 5);

            if (searchFocused) {
                int accent = purpleShift(1.0f, 0f);
                b.roundRect((int) (listX - 1), (int) (listY - 1),
                        (int) (listW + 2), (int) (SEARCH_HEIGHT + 2),
                        (100 << 24) | (accent & 0x00FFFFFF), 7, 4);
            }

            if (bFont != null) {
                String placeholder = "Поиск...";
                String display = searchQuery.isEmpty() && !searchFocused ? placeholder : searchQuery;
                int textColor = searchQuery.isEmpty() && !searchFocused ? 0xFF888888 : 0xFFFFFFFF;
                b.drawText(bFont, display, listX + 8, listY + (SEARCH_HEIGHT - 10) / 2f, 10f, textColor, 6);
                if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
                    float cursorX = listX + 8 + font.getStringWidth(searchQuery);
                    b.rect((int) cursorX, (int) (listY + 5), 1, 12, 0xFFFFFFFF, 6);
                }
            }

            float modListY = listY + SEARCH_HEIGHT + 6;
            float modListH = listH - SEARCH_HEIGHT - 6;

            float rowY = modListY + scrollOffset;
            for (ModuleRow row : rows) {
                float h = row.getCurrentHeight() + ROW_GAP;
                if (rowY + h < modListY || rowY > modListY + modListH) { rowY += h; continue; }
                row.render(context, b, bFont, listX, rowY, listW, mouseX, mouseY);
                rowY += h;
            }

            float totalH = 0;
            for (ModuleRow row : rows) totalH += row.getCurrentHeight() + ROW_GAP;
            if (totalH > modListH) {
                float barH = (modListH / totalH) * modListH;
                float barY = modListY + (-scrollOffset / totalH) * modListH;
                b.roundRect((int) (listX + listW - 4), (int) barY, 3, (int) barH, 0xAA_AA_AA_BE, 2, 5);
            }
            scrollOffset += (targetScroll - scrollOffset) * 0.25f;
        }

        b.flush(context);
    }
    private int getAccent() {
        try {
            return instance.getModuleManager().getModule(HUD.class).getColor();
        } catch (Exception e) {
            return new Color(90, 120, 255).getRGB();
        }
    }

    private static int darken(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        float scale = 0.92f + 0.08f * openAnimation.getValue().floatValue();
        float drawW = WINDOW_WIDTH * scale;
        float drawH = WINDOW_HEIGHT * scale;
        float drawX = (width - drawW) / 2f;
        float drawY = (height - drawH) / 2f;

        var font = instance.getFontManager().getBoldFont(16);

        // сайдбар
        float sidebarX = drawX + PADDING;
        float sidebarY = drawY + PADDING;
        float catStartY = sidebarY + 40;
        float catY = catStartY;

        for (Category c : Category.values()) {
            float catW = SIDEBAR_WIDTH - PADDING * 2;
            if (click.x() >= sidebarX && click.x() <= sidebarX + catW
                    && click.y() >= catY && click.y() <= catY + CAT_HEIGHT - 4) {
                if (selectedCategory != c) {
                    selectedCategory = c;
                    configMode = false;
                    rebuildRows();
                }
                return true;
            }
            catY += CAT_HEIGHT;
        }

        // Configs
        float bottomY = drawY + drawH - PADDING - 56;
        if (click.x() >= sidebarX && click.x() <= sidebarX + SIDEBAR_WIDTH - PADDING * 2
                && click.y() >= bottomY && click.y() <= bottomY + 22) {
            configMode = !configMode;
            if (configMode) refreshConfigs();
            else rebuildRows();
            return true;
        }

        // Accounts
        if (click.x() >= sidebarX && click.x() <= sidebarX + SIDEBAR_WIDTH - PADDING * 2
                && click.y() >= bottomY + 26 && click.y() <= bottomY + 48) {
            mc.setScreen(new cn.remix.ui.screen.impl.AccountManagerScreen(this));
            return true;
        }

        // список
        float listX = drawX + SIDEBAR_WIDTH + PADDING;
        float listY = drawY + PADDING;
        float listW = drawW - SIDEBAR_WIDTH - PADDING * 2;
        float listH = drawH - PADDING * 2;

        if (configMode) {
            for (int i = 0; i < configNames.size(); i++) {
                float y = listY + i * (CONFIG_ROW_HEIGHT + ROW_GAP);
                if (y + CONFIG_ROW_HEIGHT > listY + listH - 40) break;
                if (click.x() >= listX && click.x() <= listX + listW
                        && click.y() >= y && click.y() <= y + CONFIG_ROW_HEIGHT) {
                    selectedConfigIndex = i;
                    return true;
                }
            }

            float btnY = listY + listH - 36;
            float btnW = (listW - 18) / 4f;
            String[] actions = {"save", "load", "create", "delete"};
            for (int i = 0; i < 4; i++) {
                float bx = listX + i * (btnW + 6);
                if (click.x() >= bx && click.x() <= bx + btnW && click.y() >= btnY && click.y() <= btnY + 24) {
                    String selected = (selectedConfigIndex >= 0 && selectedConfigIndex < configNames.size())
                            ? configNames.get(selectedConfigIndex) : "Default";
                    ConfigManager cm = instance.getConfigManager();
                    switch (actions[i]) {
                        case "save" -> cm.saveConfig(selected);
                        case "load" -> cm.loadConfig(selected);
                        case "create" -> {
                            cm.createConfig("config_" + System.currentTimeMillis());
                            refreshConfigs();
                        }
                        case "delete" -> {
                            cm.deleteConfig(selected);
                            refreshConfigs();
                        }
                    }
                    return true;
                }
            }
        } else {
            float searchY = listY;
            if (click.x() >= listX && click.x() <= listX + listW
                    && click.y() >= searchY && click.y() <= searchY + SEARCH_HEIGHT) {
                searchFocused = true;
                return true;
            } else {
                searchFocused = false;
            }

            float modListY = listY + SEARCH_HEIGHT + 6;
            float modListH = listH - SEARCH_HEIGHT - 6;

            float rowY = modListY + scrollOffset;
            for (ModuleRow row : rows) {
                float h = row.getCurrentHeight() + ROW_GAP;
                if (rowY + h < modListY || rowY > modListY + modListH) { rowY += h; continue; }
                if (row.mouseClicked(click, listX, rowY, listW)) return true;
                rowY += h;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        for (ModuleRow row : rows) row.mouseReleased(click.x(), click.y(), click.button());
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (configMode) return true;
        float listH = WINDOW_HEIGHT - PADDING * 2 - SEARCH_HEIGHT - 6;
        float totalH = 0;
        for (ModuleRow row : rows) totalH += row.getCurrentHeight() + ROW_GAP;
        if (totalH > listH) {
            targetScroll += verticalAmount * 20;
            targetScroll = Math.max(listH - totalH, Math.min(0, targetScroll));
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        for (ModuleRow row : rows) {
            if (row.isBinding()) { row.finishBind(input.key()); return true; }
        }

        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (searchFocused) {
                searchFocused = false;
                searchQuery = "";
                rebuildRows();
                return true;
            }
            close();
            return true;
        }

        if (searchFocused) {
            if (input.key() == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    rebuildRows();
                }
                return true;
            }
            if (input.key() == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused && input.isValidChar()) {
            searchQuery += input.asString();
            rebuildRows();
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public void close() {
        instance.getModuleManager().getModule(ClickGui.class).setEnabled(false);
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
    private static final class ModuleRow implements IMinecraft {
        private final Module module;
        private final List<Component> components = new ArrayList<>();
        private final EasingAnimation expandAnimation = new EasingAnimation(Easing.EASE_OUT_CUBIC, 220);
        private final EasingAnimation hoverAnimation = new EasingAnimation(Easing.EASE_OUT_CUBIC, 150);
        private boolean extended;
        private boolean binding;

        ModuleRow(Module module) {
            this.module = module;
            for (var value : module.getValues()) {
                if (value instanceof cn.remix.module.value.impl.BoolValue bool)
                    components.add(new cn.remix.ui.clickgui.component.impl.BoolComponent(module, bool));
                else if (value instanceof cn.remix.module.value.impl.NumberValue num)
                    components.add(new cn.remix.ui.clickgui.component.impl.NumberComponent(module, num));
                else if (value instanceof cn.remix.module.value.impl.ModeValue mode)
                    components.add(new cn.remix.ui.clickgui.component.impl.ModeComponent(module, mode));
                else if (value instanceof cn.remix.module.value.impl.MultiBoolValue multi)
                    components.add(new cn.remix.ui.clickgui.component.impl.MultiBoolComponent(module, multi));
                else if (value instanceof cn.remix.module.value.impl.ColorValue color)
                    components.add(new cn.remix.ui.clickgui.component.impl.ColorComponent(module, color));
                else if (value instanceof cn.remix.module.value.impl.StringValue str)
                    components.add(new cn.remix.ui.clickgui.component.impl.StringComponent(module, str));
            }
        }

        boolean isBinding() { return binding; }

        void finishBind(int key) {
            module.setKey(key == GLFW.GLFW_KEY_ESCAPE ? -1 : key);
            binding = false;
        }

        void render(DrawContext context, BRender b, BFont bFont,
                    float x, float y, float width, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + ROW_HEIGHT;

            hoverAnimation.run(hovered ? 1 : 0);
            float hover = hoverAnimation.getValue().floatValue();

            if (module.isEnabled()) {
                // фиолетовое переливание для активного модуля
                int c1 = purpleShift(1.0f, 0f);
                b.roundRect((int) x, (int) y, (int) width, (int) ROW_HEIGHT,
                        (c1 & 0x00FFFFFF) | 0xFF000000, 5, 10);
                float pulse = (float) (0.5f + 0.5f * Math.sin(System.currentTimeMillis() / 500.0));
                int glow = (int) (40 + 30 * pulse);
                b.roundRect((int) (x - 2), (int) (y - 2), (int) (width + 4), (int) (ROW_HEIGHT + 4),
                        (glow << 24) | (c1 & 0x00FFFFFF), 7, 9);
            } else {
                int bg = ColorUtil.interpolate(
                        new Color(30, 26, 36, 210).getRGB(),
                        new Color(54, 44, 64, 235).getRGB(),
                        hover);
                b.roundRect((int) x, (int) y, (int) width, (int) ROW_HEIGHT, bg, 5, 10);
            }

            if (bFont != null) {
        String label = binding
            ? "Бинд: " + KeyUtil.getKeyName(module.getKey())
            : Translator.module(module.getName());
    b.drawText(bFont, label, x + 10, y + 6, 10f,
            binding ? 0xFFFFCC55 : 0xFFFFFFFF, 11);

    // описание
    if (!module.getDescription().isEmpty() && !binding) {
        b.drawText(bFont, module.getDescription(), x + 10, y + 19, 8f, 0xFFAAAAAA, 11);
    }

    if (!components.isEmpty() && !binding) {
        b.drawText(bFont, extended ? "▼" : "▶",
                x + width - 28, y + 7, 10f, 0xFFDDDDEE, 11);
    }
}

            expandAnimation.run(extended ? 1 : 0);
            float progress = expandAnimation.getValue().floatValue();

            float totalComponents = 0;
            for (Component c : components) {
                float ch = c.getHeight();
                if (ch >= 0.5f) totalComponents += ch;
            }

            float animH = totalComponents * progress;
            if (animH > 0.5f) {
                float offset = 4;
                for (Component c : components) {
                    float h = c.getHeight();
                    if (h < 0.5f) continue;
                    c.render(context, b, bFont, x + 6, y + ROW_HEIGHT + offset, width - 12, mouseX, mouseY, 1.0f);
                    offset += h * progress;
                }
            }
        }

        boolean mouseClicked(Click click, float x, float y, float width) {
            if (click.x() >= x && click.x() <= x + width
                    && click.y() >= y && click.y() <= y + ROW_HEIGHT) {
                if (click.x() >= x + width - 35) {
                    extended = !extended;
                    return true;
                }
                if (click.button() == 0) module.toggle();
                else if (click.button() == 1) extended = !extended;
                else if (click.button() == 2) binding = true;
                return true;
            }
            if (extended) {
                float offset = 4;
                for (Component c : components) {
                    float h = c.getHeight();
                    if (h < 0.5f) continue;
                    if (click.y() >= y + ROW_HEIGHT + offset && click.y() <= y + ROW_HEIGHT + offset + h) {
                        c.mouseClicked(click.x(), click.y(), click.button());
                        return true;
                    }
                    offset += h;
                }
            }
            return false;
        }

        void mouseReleased(double mouseX, double mouseY, int button) {
            for (Component c : components) {
                c.mouseReleased(mouseX, mouseY, button);
            }
        }

        float getCurrentHeight() {
            float h = ROW_HEIGHT + 4;
            if (extended) {
                for (Component c : components) {
                    float ch = c.getHeight();
                    if (ch >= 0.5f) h += ch;
                }
            }
            return h;
        }
    }
}