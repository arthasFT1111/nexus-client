package cn.remix.util.render;

import cn.remix.util.IMinecraft;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public final class LiquidGlassUtil implements IMinecraft {

    private LiquidGlassUtil() {
    }

    public static boolean isGlass() {
        return false;
    }

    public static void drawGlass(DrawContext context, float x, float y, float width, float height) {
        // эффект стекла отключён
    }

    public static void drawGlow(DrawContext context, float x, float y, float width, float height, float radius, long time) {
        // эффект свечения отключён
    }

    public static void drawRoundedGradient(DrawContext context, float x, float y, float width, float height,
                                           float radius, int topColor, int bottomColor) {
        if (width <= 0 || height <= 0) return;
        radius = Math.max(0.0f, Math.min(radius, Math.min(width, height) * 0.5f));

        int mid = mix(topColor, bottomColor, 0.5f);

        Render2D.drawGradient(context, x + radius, y, width - radius * 2, height, topColor, bottomColor, false);
        Render2D.drawRect(context, x, y + radius, radius, height - radius * 2, mid);
        Render2D.drawRect(context, x + width - radius, y + radius, radius, height - radius * 2, mid);
        Render2D.drawGradient(context, x + radius, y, width - radius * 2, radius, topColor, mid, false);
        Render2D.drawGradient(context, x + radius, y + height - radius, width - radius * 2, radius, mid, bottomColor, false);
        Render2D.drawArc(context, x + radius, y + radius, radius, 180, 270, mid);
        Render2D.drawArc(context, x + width - radius, y + radius, radius, 270, 360, mid);
        Render2D.drawArc(context, x + width - radius, y + height - radius, radius, 0, 90, mid);
        Render2D.drawArc(context, x + radius, y + height - radius, radius, 90, 180, mid);
    }

    private static int mix(int colorA, int colorB, float t) {
        int a1 = (colorA >>> 24) & 0xFF, r1 = (colorA >> 16) & 0xFF, g1 = (colorA >> 8) & 0xFF, b1 = colorA & 0xFF;
        int a2 = (colorB >>> 24) & 0xFF, r2 = (colorB >> 16) & 0xFF, g2 = (colorB >> 8) & 0xFF, b2 = colorB & 0xFF;
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}