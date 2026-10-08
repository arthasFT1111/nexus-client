package cn.remix.module.impl.render;

import cn.remix.module.Category;
import cn.remix.module.Module;
import cn.remix.module.value.impl.BoolValue;
import cn.remix.module.value.impl.ColorValue;
import cn.remix.module.value.impl.NumberValue;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;

import dev.bsprout.brapi.client.BFont;
import dev.bsprout.brapi.client.BRender;
import dev.bsprout.brapi.client.Gradient;
import dev.bsprout.brapi.client.GradientDirection;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Watermark extends Module {

    private final BoolValue showNickname = new BoolValue("Show Nickname", true);
    private final BoolValue showFps = new BoolValue("Show FPS", true);
    private final BoolValue showCoords = new BoolValue("Show Coords", true);
    private final BoolValue showPing = new BoolValue("Show Ping", true);
    private final BoolValue showTime = new BoolValue("Show Time", true);

    private final NumberValue posX = new NumberValue("Position X", 5, 0, 2000);
    private final NumberValue posY = new NumberValue("Position Y", 5, 0, 2000);

    private final ColorValue color1 = new ColorValue("Color 1", new Color(40, 40, 80));
    private final ColorValue color2 = new ColorValue("Color 2", new Color(15, 15, 30));
    private final NumberValue backgroundAlpha = new NumberValue("Background Alpha", 200, 0, 255);
    private final NumberValue rounding = new NumberValue("Rounding", 4, 0, 12);
    private final NumberValue textSize = new NumberValue("Text Size", 12, 6, 24);

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private BFont bFont;

    public Watermark() {
        super("Watermark", Category.Render);
    }

    @Override
    public void onEnable() {
        if (bFont == null) {
            try {
                bFont = new BFont(Identifier.of("brapi", "fonts/noto_sans_regular.ttf"));
            } catch (Exception e) {
                bFont = null;
            }
        }
    }

    /** Вызывается из HUD-элемента. */
    public void renderWatermark(DrawContext graphics) {
        if (mc.player == null || mc.world == null) return;
        if (bFont == null) return;

        StringBuilder sb = new StringBuilder("Nexus");

        if (showNickname.getValue()) {
            sb.append(" | ").append(mc.player.getName().getString());
        }
        if (showFps.getValue()) {
            sb.append(" | ").append(mc.getCurrentFps()).append(" FPS");
        }
        if (showCoords.getValue()) {
            sb.append(" | ")
              .append((int) mc.player.getX()).append(" ")
              .append((int) mc.player.getY()).append(" ")
              .append((int) mc.player.getZ());
        }
        if (showPing.getValue()) {
            int ping = 0;
            if (mc.getNetworkHandler() != null) {
                PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (entry != null) ping = entry.getLatency();
            }
            sb.append(" | ").append(ping).append(" ms");
        }
        if (showTime.getValue()) {
            sb.append(" | ").append(LocalTime.now().format(TIME_FORMAT));
        }

        String text = sb.toString();

        int x = posX.getValue().intValue();
        int y = posY.getValue().intValue();
        float size = textSize.getValue();
        int pad = 4;
        int radius = rounding.getValue().intValue();

        float textWidth = bFont.textSize(text, size);
        float textHeight = size + 2;

        int alpha = backgroundAlpha.getValue().intValue();
        int c1 = applyAlpha(color1.getValue().getRGB(), alpha);
        int c2 = applyAlpha(color2.getValue().getRGB(), alpha);

        BRender b = new BRender();

        Gradient gradient = new Gradient(c1, c2);
        b.roundRect(
                x - pad, y - pad,
                (int) (textWidth + pad * 2), (int) (textHeight + pad * 2),
                gradient, GradientDirection.LEFT_RIGHT,
                radius, 0
        );

        b.drawText(bFont, text, x, y, size, 0xFFFFFFFF, 1);

        b.flush(graphics);
    }

    private static int applyAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}