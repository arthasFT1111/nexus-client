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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class CelestialClickGuiScreen extends Screen implements IMinecraft {

    private static final float WINDOW_WIDTH = 500;
    private static final float WINDOW_HEIGHT = 320;
    private static final float TAB_HEIGHT = 30;
    private static final float SEARCH_HEIGHT = 22;
    private static final float ROW_HEIGHT = 20;
    private static final float ROW_GAP = 3;
    private static final float PADDING = 12;
    private static final float CONFIG_ROW_HEIGHT = 22;
    private static final float CONFIG_BTN_WIDTH = 70;

    private Category selectedCategory = Category.Combat;
    private boolean configMode = false;

    private final List<ModuleRow> rows = new ArrayList<>();
    private final List<String> configNames = new ArrayList<>();
    private int selectedConfigIndex = -1;

    private float scrollOffset = 0;
    private float targetScroll = 0;

    private float windowX, windowY;

    private final EasingAnimation openAnimation = new EasingAnimation(Easing.EASE_OUT_CUBIC, 260);

    private BFont bFont;

    private String searchQuery = "";
    private boolean searchFocused = false;

    public CelestialClickGuiScreen() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        windowX = (width - WINDOW_WIDTH) / 2f;
        windowY = (height - WINDOW_HEIGHT) / 2f;
        openAnimation.reset();

        if (bFont == null) {
            try {
                bFont = new BFont(Identifier.of("brapi", "fonts/noto_sans_regular.ttf"));
            } catch (Exception e) {
                bFont = null;
            }
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
                if (!original.contains(query) && !translated.contains(query)) continue;
            }
            rows.add(new ModuleRow(m));
        }
        scrollOffset = 0;
        targetScroll = 0;
    }

    private static int rainbowShift(float speed, float saturation, float brightness, float offset) {
        float hue = ((System.currentTimeMillis() % (long) (speed * 1000)) / (speed * 1000f) + offset) % 1.0f;
        return Color.HSBtoRGB(hue, saturation, brightness);
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

        for (int i = 10; i > 0; i--) {
            int shadowAlpha = (int) (14 * (1.0f - i / 10.0f) * open);
            if (shadowAlpha <= 0) continue;
            b.roundRect((int) (drawX - i), (int) (drawY - i), (int) (drawW + i * 2), (int) (drawH + i * 2),
                    (shadowAlpha << 24), 12 + i, 1);
        }

        int b1 = rainbowShift(8f, 0.55f, 1.0f, 0f);
        for (int i = 0; i < 2; i++) {
            int alpha = i == 0 ? 110 : 50;
            b.roundRect((int) (drawX - i - 1), (int) (drawY - i - 1),
                    (int) (drawW + (i + 1) * 2), (int) (drawH + (i + 1) * 2),
                    (alpha << 24) | (b1 & 0x00FFFFFF), 12 + i + 1, 1);
        }

        int f1 = darken(rainbowShift(10f, 0.35f, 0.16f, 0f), 1.0f);
        int f2 = darken(rainbowShift(10f, 0.35f, 0.10f, 0.5f), 1.0f);
        b.roundRect((int) drawX, (int) drawY, (int) drawW, (int) drawH,
                (int) (245 * open) << 24 | (f1 & 0x00FFFFFF), 12, 2);

        b.roundRect((int) (drawX + 1), (int) (drawY + 1), (int) (drawW - 2), 40,
                (int) (28 * open) << 24 | 0x00FFFFFF, 12, 3);

        b.rect((int) (drawX + 12), (int) (drawY + 1), (int) (drawW - 24), 1,
                (int) (60 * open) << 24 | 0x00FFFFFF, 4);

        var font = instance.getFontManager().getBoldFont(16);
        float tabX = drawX + PADDING;
        float tabY = drawY + PADDING;
        float tabH = TAB_HEIGHT - 10;

        if (!configMode) {
            for (Category c : Category.values()) {
                String icon = categoryIcon(c);
                String translatedName = Translator.category(c.getName());
                String label = (icon.isEmpty() ? "" : icon + "  ") + translatedName;
                float tabWidth = font.getStringWidth(label) + 20;
                boolean active = c == selectedCategory;
                boolean hovered = mouseX >= tabX && mouseX <= tabX + tabWidth
                        && mouseY >= tabY && mouseY <= tabY + tabH;

                if (active) {
                    int c1 = rainbowShift(8f, 0.55f, 1.0f, 0f);
                    b.roundRect((int) tabX, (int) tabY, (int) tabWidth, (int) tabH,
                            (c1 & 0x00FFFFFF) | 0xFF000000, 7, 5);
                    float pulse = (float) (0.5f + 0.5f * Math.sin(System.currentTimeMillis() / 400.0));
                    int glowAlpha = (int) (60 + 50 * pulse);
                    b.roundRect((int) (tabX - 2), (int) (tabY - 2), (int) (tabWidth + 4), (int) (tabH + 4),
                            (glowAlpha << 24) | (c1 & 0x00FFFFFF), 9, 4);
                } else {
                    int bg = hovered ? new Color(50, 50, 58, 230).getRGB() : new Color(28, 28, 34, 210).getRGB();
                    b.roundRect((int) tabX, (int) tabY, (int) tabWidth, (int) tabH, bg, 7, 5);
                }

                if (bFont != null) {
                    float textX = tabX + (tabWidth - font.getStringWidth(label)) / 2f;
                    float textY = tabY + (tabH - 10) / 2f + 0.5f;
                    b.drawText(bFont, label, textX, textY, 10f, active ? 0xFFFFFFFF : 0xFFAAAAAA, 6);
                }
                tabX += tabWidth + 5;
            }
        }

        // Кнопка Configs
        float configBtnX = drawX + drawW - PADDING - CONFIG_BTN_WIDTH;
        float configBtnY = drawY + PADDING + TAB_HEIGHT;
        boolean configHovered = mouseX >= configBtnX && mouseX <= configBtnX + CONFIG_BTN_WIDTH
                && mouseY >= configBtnY && mouseY <= configBtnY + SEARCH_HEIGHT;

        int configBg;
        if (configMode) configBg = (rainbowShift(8f, 0.55f, 1.0f, 0f) & 0x00FFFFFF) | 0xFF000000;
        else if (configHovered) configBg = new Color(50, 50, 58, 230).getRGB();
        else configBg = new Color(28, 28, 34, 210).getRGB();

        b.roundRect((int) configBtnX, (int) configBtnY, (int) CONFIG_BTN_WIDTH, (int) SEARCH_HEIGHT,
                configBg, 6, 5);

        if (bFont != null) {
            String configLabel = "Configs";
            float textX = configBtnX + (CONFIG_BTN_WIDTH - font.getStringWidth(configLabel)) / 2f;
            float textY = configBtnY + (SEARCH_HEIGHT - 10) / 2f + 0.5f;
            b.drawText(bFont, configLabel, textX, textY, 10f, 0xFFFFFFFF, 6);
        }

        float searchY = drawY + PADDING + TAB_HEIGHT;
        float searchW = drawW - PADDING * 2 - CONFIG_BTN_WIDTH - 6;

        if (!configMode) {
            int searchBg = searchFocused ? new Color(40, 40, 48, 245).getRGB() : new Color(28, 28, 34, 220).getRGB();
            b.roundRect((int) (drawX + PADDING), (int) searchY, (int) searchW, (int) SEARCH_HEIGHT, searchBg, 6, 5);

            if (searchFocused) {
                int accent = rainbowShift(8f, 0.55f, 1.0f, 0f);
                b.roundRect((int) (drawX + PADDING - 1), (int) (searchY - 1),
                        (int) (searchW + 2), (int) (SEARCH_HEIGHT + 2),
                        (100 << 24) | (accent & 0x00FFFFFF), 7, 4);
            }

            if (bFont != null) {
                String placeholder = "Поиск...";
                String display = searchQuery.isEmpty() && !searchFocused ? placeholder : searchQuery;
                int textColor = searchQuery.isEmpty() && !searchFocused ? 0xFF888888 : 0xFFFFFFFF;
                b.drawText(bFont, display, drawX + PADDING + 8,
                        searchY + (SEARCH_HEIGHT - 10) / 2f, 10f, textColor, 6);
                if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
                    float cursorX = drawX + PADDING + 8 + font.getStringWidth(searchQuery);
                    b.rect((int) cursorX, (int) (searchY + 5), 1, 12, 0xFFFFFFFF, 6);
                }
            }

            float listX = drawX + PADDING;
            float listY = searchY + SEARCH_HEIGHT + 4;
            float listW = drawW - PADDING * 2;
            float listH = drawH - PADDING * 2 - TAB_HEIGHT - SEARCH_HEIGHT - 4;

            float rowY = listY + scrollOffset;
            for (ModuleRow row : rows) {
                float h = row.getCurrentHeight() + ROW_GAP;
                if (rowY + h < listY || rowY > listY + listH) { rowY += h; continue; }
                row.render(context, b, bFont, listX, rowY, listW, mouseX, mouseY);
                rowY += h;
            }

            float totalH = 0;
            for (ModuleRow row : rows) totalH += row.getCurrentHeight() + ROW_GAP;
            if (totalH > listH) {
                float barH = (listH / totalH) * listH;
                float barY = listY + (-scrollOffset / totalH) * listH;
                b.roundRect((int) (listX + listW - 4), (int) barY, 3, (int) barH, 0xAA_AA_AA_BE, 2, 5);
            }
            scrollOffset += (targetScroll - scrollOffset) * 0.25f;
        } else {
            float listX = drawX + PADDING;
            float listY = searchY;
            float listW = drawW - PADDING * 2;
            float listH = drawH - PADDING * 2 - TAB_HEIGHT;

            for (int i = 0; i < configNames.size(); i++) {
                String name = configNames.get(i);
                float y = listY + i * (CONFIG_ROW_HEIGHT + ROW_GAP);
                if (y + CONFIG_ROW_HEIGHT > listY + listH - 40) break;

                boolean selected = i == selectedConfigIndex;
                boolean hovered = mouseX >= listX && mouseX <= listX + listW
                        && mouseY >= y && mouseY <= y + CONFIG_ROW_HEIGHT;

                int bg;
                if (selected) bg = (rainbowShift(8f, 0.55f, 1.0f, 0f) & 0x00FFFFFF) | 0xFF000000;
                else if (hovered) bg = new Color(50, 50, 58, 230).getRGB();
                else bg = new Color(30, 30, 36, 210).getRGB();

                b.roundRect((int) listX, (int) y, (int) listW, (int) CONFIG_ROW_HEIGHT, bg, 5, 5);

                if (bFont != null) {
                    b.drawText(bFont, name, listX + 10, y + (CONFIG_ROW_HEIGHT - 10) / 2f + 0.5f,
                            10f, 0xFFFFFFFF, 6);
                }
            }

            float btnY = listY + listH - 36;
            float btnW = (listW - 18) / 4f;
            String[] labels = {"Сохранить", "Загрузить", "Создать", "Удалить"};
            for (int i = 0; i < 4; i++) {
                float bx = listX + i * (btnW + 6);
                boolean hovered = mouseX >= bx && mouseX <= bx + btnW && mouseY >= btnY && mouseY <= btnY + 24;
                int bg = hovered ? new Color(60, 60, 70, 240).getRGB() : new Color(35, 35, 42, 220).getRGB();
                b.roundRect((int) bx, (int) btnY, (int) btnW, 24, bg, 5, 5);
                if (bFont != null) {
                    float tw = font.getStringWidth(labels[i]);
                    b.drawText(bFont, labels[i], bx + (btnW - tw) / 2f, btnY + 7, 10f, 0xFFFFFFFF, 6);
                }
            }
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
        float tabX = drawX + PADDING;
        float tabY = drawY + PADDING;
        float tabH = TAB_HEIGHT - 10;

        float configBtnX = drawX + drawW - PADDING - CONFIG_BTN_WIDTH;
        float configBtnY = drawY + PADDING + TAB_HEIGHT;
        if (click.x() >= configBtnX && click.x() <= configBtnX + CONFIG_BTN_WIDTH
                && click.y() >= configBtnY && click.y() <= configBtnY + SEARCH_HEIGHT) {
            configMode = !configMode;
            if (configMode) refreshConfigs();
            else rebuildRows();
            return true;
        }

        if (!configMode) {
            for (Category c : Category.values()) {
                String icon = categoryIcon(c);
                String label = (icon.isEmpty() ? "" : icon + "  ") + Translator.category(c.getName());
                float tabWidth = font.getStringWidth(label) + 20;
                if (click.x() >= tabX && click.x() <= tabX + tabWidth
                        && click.y() >= tabY && click.y() <= tabY + tabH) {
                    if (selectedCategory != c) {
                        selectedCategory = c;
                        rebuildRows();
                    }
                    return true;
                }
                tabX += tabWidth + 5;
            }
        }

        float searchY = drawY + PADDING + TAB_HEIGHT;
        float searchW = drawW - PADDING * 2 - CONFIG_BTN_WIDTH - 6;

        if (!configMode) {
            if (click.x() >= drawX + PADDING && click.x() <= drawX + PADDING + searchW
                    && click.y() >= searchY && click.y() <= searchY + SEARCH_HEIGHT) {
                searchFocused = true;
                return true;
            } else {
                searchFocused = false;
            }

            float listX = drawX + PADDING;
            float listY = searchY + SEARCH_HEIGHT + 4;
            float listW = drawW - PADDING * 2;
            float listH = drawH - PADDING * 2 - TAB_HEIGHT - SEARCH_HEIGHT - 4;

            float rowY = listY + scrollOffset;
            for (ModuleRow row : rows) {
                float h = row.getCurrentHeight() + ROW_GAP;
                if (rowY + h < listY || rowY > listY + listH) { rowY += h; continue; }
                if (row.mouseClicked(click, listX, rowY, listW)) return true;
                rowY += h;
            }
        } else {
            float listX = drawX + PADDING;
            float listY = searchY;
            float listW = drawW - PADDING * 2;
            float listH = drawH - PADDING * 2 - TAB_HEIGHT;

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
        float listH = WINDOW_HEIGHT - PADDING * 2 - TAB_HEIGHT - SEARCH_HEIGHT - 4;
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
            var font = instance.getFontManager().getBoldFont(15);
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + ROW_HEIGHT;

            hoverAnimation.run(hovered ? 1 : 0);
            float hover = hoverAnimation.getValue().floatValue();

            if (module.isEnabled()) {
                int c1 = Color.HSBtoRGB((System.currentTimeMillis() % 8000) / 8000f, 0.55f, 0.95f);
                b.roundRect((int) x, (int) y, (int) width, (int) ROW_HEIGHT,
                        (c1 & 0x00FFFFFF) | 0xFF000000, 5, 10);
                float pulse = (float) (0.5f + 0.5f * Math.sin(System.currentTimeMillis() / 500.0));
                int glow = (int) (40 + 30 * pulse);
                b.roundRect((int) (x - 2), (int) (y - 2), (int) (width + 4), (int) (ROW_HEIGHT + 4),
                        (glow << 24) | (c1 & 0x00FFFFFF), 7, 9);
            } else {
                int bg = ColorUtil.interpolate(
                        new Color(30, 30, 36, 210).getRGB(),
                        new Color(54, 54, 62, 235).getRGB(),
                        hover);
                b.roundRect((int) x, (int) y, (int) width, (int) ROW_HEIGHT, bg, 5, 10);
            }

            if (bFont != null) {
                String label = binding
                        ? "Бинд: " + KeyUtil.getKeyName(module.getKey())
                        : Translator.module(module.getName());
                b.drawText(bFont, label, x + 10, y + (ROW_HEIGHT - 10) / 2f + 0.5f, 10f,
                        binding ? 0xFFFFCC55 : 0xFFFFFFFF, 11);

                if (!components.isEmpty() && !binding) {
                    b.drawText(bFont, extended ? "-" : "+",
                            x + width - 12, y + (ROW_HEIGHT - 10) / 2f + 0.5f, 10f, 0xFFDDDDEE, 11);
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