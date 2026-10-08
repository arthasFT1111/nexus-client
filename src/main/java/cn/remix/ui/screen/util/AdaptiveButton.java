package cn.remix.ui.screen.util;

import cn.remix.util.IMinecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import dev.bsprout.brapi.client.BFont;
import dev.bsprout.brapi.client.BRender;

public class AdaptiveButton implements IMinecraft {
    private float x, y, width, height;
    private final String text;
    private final Runnable action;

    private float hoverProgress = 0.0f;
    private float appearProgress = 0.0f;

    private static BFont bFont;

    public AdaptiveButton(String text, Runnable action) {
        this.text = text;
        this.action = action;
        if (bFont == null) {
            try {
                bFont = new BFont(Identifier.of("brapi", "fonts/noto_sans_regular.ttf"));
            } catch (Exception ignored) {}
        }
    }

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public void setY(float y) { this.y = y; }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (appearProgress < 1.0f) {
            appearProgress += delta * 0.05f;
            if (appearProgress > 1.0f) appearProgress = 1.0f;
        }
        float appear = appearProgress;

        boolean hovered = isHovered(mouseX, mouseY);
        float targetHover = hovered ? 1.0f : 0.0f;
        hoverProgress += (targetHover - hoverProgress) * Math.min(1.0f, 0.12f * delta);

        float offsetY = (1f - appear) * 20f;
        int alpha = (int) (255 * appear);

        // фон: от тёмно-фиолетового к яркому фиолетовому при hover
        int r = (int) (25 + hoverProgress * 35);
        int g = (int) (15 + hoverProgress * 20);
        int b = (int) (45 + hoverProgress * 60);
        int bgColor = (alpha << 24) | (r << 16) | (g << 8) | b;

        // текст: от серого к белому
        int textGray = (int) (170 + hoverProgress * 85);
        int textColor = (alpha << 24) | (textGray << 16) | (textGray << 8) | textGray;

        // Brapi-рендер
        BRender br = new BRender();
        br.roundRect((int) x, (int) (y + offsetY), (int) width, (int) height, bgColor, 8, 0);

        if (bFont != null) {
            float textWidth = bFont.textSize(text, 10f);
            float textX = x + (width - textWidth) / 2f;
            float textY = y + offsetY + (height - 10) / 2f + 0.5f;
            br.drawText(bFont, text, textX, textY, 10f, textColor, 1);
        }

        br.flush(context);
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        render(context, mouseX, mouseY, 1.0f);
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void onClick() {
        if (action != null) {
            action.run();
        }
    }
}