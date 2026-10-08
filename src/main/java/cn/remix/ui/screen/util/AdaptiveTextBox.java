package cn.remix.ui.screen.util;

import cn.remix.ui.font.TrueTypeFont;
import cn.remix.util.IMinecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class AdaptiveTextBox implements IMinecraft {
    private float x, y, width, height;
    private String text = "";
    private final String placeholder;
    private boolean focused = false;
    private boolean passwordMode = false;
    private int cursorPosition = 0;

    public AdaptiveTextBox(String placeholder) {
        this.placeholder = placeholder;
    }

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
        this.cursorPosition = this.text.length();
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public void setPasswordMode(boolean passwordMode) {
        this.passwordMode = passwordMode;
    }

    public boolean isPasswordMode() {
        return passwordMode;
    }

    public void render(DrawContext context) {
        // защита от null (пока Client не инициализирован)
        if (instance == null || instance.getFontManager() == null) return;

        TrueTypeFont font = instance.getFontManager().getFont(16);

        // фон
        int bg = focused ? new Color(40, 30, 55, 240).getRGB() : new Color(28, 24, 34, 220).getRGB();
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), bg);

        // обводка
        int border = focused ? new Color(120, 80, 200, 255).getRGB() : new Color(60, 50, 70, 200).getRGB();
        context.fill((int) x, (int) y, (int) (x + width), (int) y + 1, border);
        context.fill((int) x, (int) (y + height - 1), (int) (x + width), (int) (y + height), border);
        context.fill((int) x, (int) y, (int) x + 1, (int) (y + height), border);
        context.fill((int) (x + width - 1), (int) y, (int) (x + width), (int) (y + height), border);

        // текст
        String display = text.isEmpty() && !focused ? placeholder : (passwordMode ? "*".repeat(text.length()) : text);
        int textColor = text.isEmpty() && !focused ? 0xFF666666 : 0xFFFFFFFF;
        float textY = y + (height - font.getHeight()) / 2f + 0.5f;
        font.drawString(context, display, x + 6, textY, textColor, false);

        // курсор
        if (focused && (System.currentTimeMillis() / 500) % 2 == 0) {
            float cursorX = x + 6 + font.getStringWidth(display);
            context.fill((int) cursorX, (int) (y + 3), (int) cursorX + 1, (int) (y + height - 3), 0xFFFFFFFF);
        }
    }

    public void mouseClicked(net.minecraft.client.gui.Click click) {
        focused = click.x() >= x && click.x() <= x + width
                && click.y() >= y && click.y() <= y + height;
    }

    public boolean charTyped(CharInput input) {
        if (!focused || !input.isValidChar()) return false;
        text += input.asString();
        cursorPosition = text.length();
        return true;
    }

    public boolean keyPressed(KeyInput input) {
        if (!focused) return false;
        if (input.key() == GLFW.GLFW_KEY_BACKSPACE) {
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
                cursorPosition = text.length();
            }
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_ENTER) {
            focused = false;
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            focused = false;
            return true;
        }
        return false;
    }
}