package cn.remix.module.impl.render;

import cn.remix.module.Category;
import cn.remix.module.Module;
import cn.remix.module.value.impl.BoolValue;
import cn.remix.module.value.impl.ColorValue;
import cn.remix.module.value.impl.NumberValue;
import cn.remix.util.animation.Easing;
import cn.remix.util.animation.EasingAnimation;
import cn.remix.util.render.ColorUtil;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import dev.bsprout.brapi.client.BFont;
import dev.bsprout.brapi.client.BRender;
import dev.bsprout.brapi.client.BTexture;
import dev.bsprout.brapi.client.Gradient;
import dev.bsprout.brapi.client.GradientDirection;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class HUD extends Module {

    // ─── Общие настройки ───
    private final ColorValue mainColor = new ColorValue("Main Color", new Color(90, 120, 255));
    private final ColorValue secondColor = new ColorValue("Second Color", new Color(160, 90, 255));
    private final BoolValue whiteMode = new BoolValue("White Mode", false);
    private final BoolValue noPotionIcons = new BoolValue("No Potion Icons", false);

    // ─── Rainbow ───
    private final BoolValue rainbowBg = new BoolValue("Rainbow BG", true);
    private final NumberValue rainbowSpeed = new NumberValue("Rainbow Speed", 6, 1, 20);

    // ─── Watermark ───
    private final BoolValue wmEnabled = new BoolValue("Watermark", true);
    private final NumberValue wmX = new NumberValue("Watermark X", 5, 0, 2000);
    private final NumberValue wmY = new NumberValue("Watermark Y", 5, 0, 2000);
    private final BoolValue wmFps = new BoolValue("Watermark FPS", true);
    private final BoolValue wmPing = new BoolValue("Watermark Ping", true);
    private final BoolValue wmTps = new BoolValue("Watermark TPS", true);
    private final BoolValue wmTime = new BoolValue("Watermark Time", true);

    // ─── Coordinates ───
    private final BoolValue coordsEnabled = new BoolValue("Coordinates", true);
    private final NumberValue coordsX = new NumberValue("Coordinates X", 5, 0, 2000);
    private final NumberValue coordsY = new NumberValue("Coordinates Y", 30, 0, 2000);

    // ─── ArmorHUD ───
    private final BoolValue armorEnabled = new BoolValue("ArmorHUD", true);
    private final NumberValue armorX = new NumberValue("Armor X", 5, 0, 2000);
    private final NumberValue armorY = new NumberValue("Armor Y", 55, 0, 2000);

    // ─── TargetHUD ───
    private final BoolValue targetEnabled = new BoolValue("TargetHUD", true);
    private final NumberValue targetX = new NumberValue("Target X", 300, 0, 2000);
    private final NumberValue targetY = new NumberValue("Target Y", 300, 0, 2000);

    // ─── Binds ───
    private final BoolValue bindsEnabled = new BoolValue("Binds", true);
    private final NumberValue bindsX = new NumberValue("Binds X", 5, 0, 2000);
    private final NumberValue bindsY = new NumberValue("Binds Y", 100, 0, 2000);

    // ─── StaffList ───
    private final BoolValue staffEnabled = new BoolValue("StaffList", true);
    private final NumberValue staffX = new NumberValue("Staff X", 400, 0, 2000);
    private final NumberValue staffY = new NumberValue("Staff Y", 5, 0, 2000);

    // ─── Notifications ───
    private final BoolValue notifEnabled = new BoolValue("Notifications", true);
    private final NumberValue notifX = new NumberValue("Notif X", 400, 0, 2000);
    private final NumberValue notifY = new NumberValue("Notif Y", 100, 0, 2000);

    // ─── Цвета ───
    private final ColorValue color1 = new ColorValue("Color 1", new Color(40, 40, 80));
    private final ColorValue color2 = new ColorValue("Color 2", new Color(15, 15, 30));
    private final ColorValue accent = new ColorValue("Accent", new Color(90, 120, 255));

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private BFont bFont;
    private BTexture logoTexture;

    private final List<HudPart> parts = new ArrayList<>();

    public HUD() {
        super("HUD", Category.Render);
    }

    @Override
    public void onEnable() {
        if (parts.isEmpty()) {
            parts.add(new WatermarkPart());
            parts.add(new CoordinatesPart());
            parts.add(new ArmorPart());
            parts.add(new TargetPart());
            parts.add(new BindsPart());
            parts.add(new StaffPart());
            parts.add(new NotifPart());
        }
    }

    // ═══════════════════════════════════════════
    // Совместимость со старым HUD
    // ═══════════════════════════════════════════

    public ColorValue getMainColor() { return mainColor; }
    public ColorValue getSecondColor() { return secondColor; }
    public BoolValue getWhiteMode() { return whiteMode; }
    public BoolValue getNoPotionIcons() { return noPotionIcons; }

    public int getColor() {
        return mainColor.getValue().getRGB();
    }

    public int getColor(int index) {
        int c1 = mainColor.getValue().getRGB();
        int c2 = secondColor.getValue().getRGB();
        float ratio = (index % 8) / 8f;
        return ColorUtil.interpolate(c1, c2, ratio);
    }

    // ═══════════════════════════════════════════
    // Рендер
    // ═══════════════════════════════════════════

    public void renderHud(DrawContext ctx) {
        if (mc.player == null || mc.world == null) return;

        if (bFont == null) {
            try {
                bFont = new BFont(Identifier.of("brapi", "fonts/noto_sans_regular.ttf"));
            } catch (Exception e) {
                bFont = null;
            }
        }
        if (logoTexture == null) {
            try {
                logoTexture = new BTexture(Identifier.of("remix", "textures/gui/logo.png"));
            } catch (Exception e) {
                logoTexture = null;
            }
        }
        if (bFont == null) return;

        boolean chatOpen = mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen;
        double mouseX = mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth();
        double mouseY = mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight();
        boolean leftDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(
                mc.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT)
                == org.lwjgl.glfw.GLFW.GLFW_PRESS;

        BRender b = new BRender();

        for (HudPart part : parts) {
            part.drag(chatOpen, leftDown, mouseX, mouseY);
            part.render(ctx, b);
        }

        b.flush(ctx);
    }

    // ═══════════════════════════════════════════
    // Rainbow helpers
    // ═══════════════════════════════════════════

    private static int rainbowShift(float speed, float saturation, float brightness, float offset) {
        float hue = ((System.currentTimeMillis() % (long) (speed * 1000)) / (speed * 1000f) + offset) % 1.0f;
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    private int[] getBgColors() {
        if (rainbowBg.getValue()) {
            float speed = rainbowSpeed.getValue();
            int c1 = applyAlpha(rainbowShift(speed, 0.55f, 0.35f, 0f), 220);
            int c2 = applyAlpha(rainbowShift(speed, 0.55f, 0.25f, 0.5f), 220);
            return new int[]{c1, c2};
        } else {
            int c1 = applyAlpha(color1.getValue().getRGB(), 220);
            int c2 = applyAlpha(color2.getValue().getRGB(), 220);
            return new int[]{c1, c2};
        }
    }

    private int getAccentColor() {
        if (rainbowBg.getValue()) {
            return rainbowShift(rainbowSpeed.getValue(), 0.55f, 1.0f, 0f);
        }
        return accent.getValue().getRGB();
    }

    // ═══════════════════════════════════════════
    // HudPart
    // ═══════════════════════════════════════════

    private abstract class HudPart {
        protected float x, y, width, height;
        protected boolean dragging;
        protected float dragOffsetX, dragOffsetY;

        abstract void render(DrawContext ctx, BRender b);
        abstract boolean isEnabled();
        abstract NumberValue posX();
        abstract NumberValue posY();

        void updatePos() {
            this.x = posX().getValue();
            this.y = posY().getValue();
        }

        void drag(boolean chatOpen, boolean leftDown, double mouseX, double mouseY) {
            updatePos();

            if (!chatOpen || !leftDown) {
                dragging = false;
                return;
            }

            boolean hovered = mouseX >= x && mouseX <= x + width
                    && mouseY >= y && mouseY <= y + height;

            if (!dragging && hovered) {
                dragging = true;
                dragOffsetX = (float) (mouseX - x);
                dragOffsetY = (float) (mouseY - y);
            }

            if (dragging) {
                float newX = (float) (mouseX - dragOffsetX);
                float newY = (float) (mouseY - dragOffsetY);
                posX().setValue(Math.max(0, Math.min(2000, newX)));
                posY().setValue(Math.max(0, Math.min(2000, newY)));
                this.x = posX().getValue();
                this.y = posY().getValue();
            }
        }

        void drawBg(BRender b, float radius) {
            int[] colors = getBgColors();
            Gradient bg = new Gradient(colors[0], colors[1]);
            b.roundRect((int) x, (int) y, (int) width, (int) height,
                    bg, GradientDirection.LEFT_RIGHT, (int) radius, 0);
        }
    }

    // ─── Watermark ───
    private class WatermarkPart extends HudPart {
        @Override boolean isEnabled() { return wmEnabled.getValue(); }
        @Override NumberValue posX() { return wmX; }
        @Override NumberValue posY() { return wmY; }

        @Override
        void render(DrawContext ctx, BRender b) {
            if (!isEnabled()) return;

            StringBuilder sb = new StringBuilder("Nexus");
            if (wmFps.getValue()) sb.append(" | ").append(mc.getCurrentFps()).append(" FPS");
            if (wmPing.getValue()) {
                int ping = 0;
                if (mc.getNetworkHandler() != null) {
                    PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                    if (entry != null) ping = entry.getLatency();
                }
                sb.append(" | ").append(ping).append(" ms");
            }
            if (wmTps.getValue()) sb.append(" | 20 TPS");
            if (wmTime.getValue()) sb.append(" | ").append(LocalTime.now().format(TIME_FORMAT));

            String text = sb.toString();
            float size = 10f;
            float textWidth = bFont.textSize(text, size);
            float iconSize = 18;
            float pad = 4;
            this.width = textWidth + iconSize + pad * 4 + 4;
            this.height = 24;

            drawBg(b, 8);

            if (logoTexture != null) {
                b.drawTexture(logoTexture, x + pad, y + pad, iconSize, iconSize, 0xFFFFFFFF, true, 2);
            } else {
                int accentColor = getAccentColor();
                Gradient iconGrad = new Gradient(accentColor, darken(accentColor, 0.7f));
                b.roundRect((int) (x + pad), (int) (y + pad), (int) iconSize, (int) iconSize,
                        iconGrad, GradientDirection.TOP_BOTTOM, 5, 1);
                b.drawText(bFont, "N", x + pad + 5, y + pad + 3.5f, 11f, 0xFFFFFFFF, 2);
            }

            b.drawText(bFont, text, x + pad + iconSize + pad + 2,
                    y + (height - 10) / 2f, size, 0xFFFFFFFF, 2);
        }
    }

    // ─── Coordinates ───
    private class CoordinatesPart extends HudPart {
        @Override boolean isEnabled() { return coordsEnabled.getValue(); }
        @Override NumberValue posX() { return coordsX; }
        @Override NumberValue posY() { return coordsY; }

        @Override
        void render(DrawContext ctx, BRender b) {
            if (!isEnabled()) return;

            String text = String.format("X: %d  Y: %d  Z: %d",
                    (int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ());
            float size = 10f;
            float textWidth = bFont.textSize(text, size);
            this.width = textWidth + 12;
            this.height = 18;

            drawBg(b, 6);
            b.drawText(bFont, text, x + 6, y + (height - 10) / 2f, size, 0xFFFFFFFF, 2);
        }
    }

    // ─── Armor ───
    private class ArmorPart extends HudPart {
        @Override boolean isEnabled() { return armorEnabled.getValue(); }
        @Override NumberValue posX() { return armorX; }
        @Override NumberValue posY() { return armorY; }

        @Override
        void render(DrawContext ctx, BRender b) {
            if (!isEnabled()) return;

            this.width = 4 * 20;
            this.height = 20;

            for (int i = 0; i < 4; i++) {
                var stack = mc.player.getInventory().getStack(36 + i);
                if (!stack.isEmpty()) {
                    ctx.drawItem(stack, (int) (x + i * 20), (int) y);
                    ctx.drawStackOverlay(mc.textRenderer, stack, (int) (x + i * 20), (int) y);
                }
            }
        }
    }

    // ─── TargetHUD ───
    private class TargetPart extends HudPart {
        private final EasingAnimation showAnim = new EasingAnimation(Easing.EASE_OUT_CUBIC, 200);
        private LivingEntity lastTarget;

        @Override boolean isEnabled() { return targetEnabled.getValue(); }
        @Override NumberValue posX() { return targetX; }
        @Override NumberValue posY() { return targetY; }

        @Override
        void render(DrawContext ctx, BRender b) {
            if (!isEnabled()) return;

            var target = mc.targetedEntity;
            LivingEntity living = (target instanceof LivingEntity le) ? le : null;

            if (living != null) lastTarget = living;

            showAnim.run(living != null ? 1 : 0);
            float show = showAnim.getValue().floatValue();

            if (show < 0.01f || lastTarget == null) return;

            int alpha = (int) (220 * show);

            this.width = 130;
            this.height = 50;

            int[] colors = getBgColors();
            Gradient bg = new Gradient(
                    applyAlpha(colors[0], alpha),
                    applyAlpha(colors[1], alpha)
            );
            b.roundRect((int) x, (int) y, (int) width, (int) height,
                    bg, GradientDirection.LEFT_RIGHT, 8, 0);

            if (lastTarget instanceof AbstractClientPlayerEntity player) {
                var skin = player.getSkin().body().texturePath();
                ctx.drawTexture(RenderPipelines.GUI_TEXTURED, skin,
                        (int) (x + 6), (int) (y + 6),
                        8f, 8f, 32, 32, 64, 64);
                ctx.drawTexture(RenderPipelines.GUI_TEXTURED, skin,
                        (int) (x + 6), (int) (y + 6),
                        40f, 8f, 32, 32, 64, 64);
            }

            b.drawText(bFont, lastTarget.getName().getString(),
                    x + 44, y + 6, 10f,
                    applyAlpha(0xFFFFFFFF, (int) (255 * show)), 2);

            float hpPercent = lastTarget.getHealth() / lastTarget.getMaxHealth();
            b.roundRect((int) (x + 44), (int) (y + 22), 80, 6,
                    applyAlpha(0xFF303030, alpha), 3, 1);
            b.roundRect((int) (x + 44), (int) (y + 22), (int) (80 * hpPercent), 6,
                    applyAlpha(getAccentColor(), alpha), 3, 2);

            String hpText = String.format("%.1f", lastTarget.getHealth());
            b.drawText(bFont, hpText,
                    x + 44, y + 32, 8f,
                    applyAlpha(0xFFFFFFFF, (int) (255 * show)), 2);

            int armorX = (int) (x + 44);
            int armorY = (int) (y + 42);
            EquipmentSlot[] slots = {
                    EquipmentSlot.HEAD,
                    EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS,
                    EquipmentSlot.FEET
            };
            for (int i = 0; i < 4; i++) {
                var stack = lastTarget.getEquippedStack(slots[i]);
                if (!stack.isEmpty()) {
                    ctx.drawItem(stack, armorX + i * 18, armorY);
                    ctx.drawStackOverlay(mc.textRenderer, stack, armorX + i * 18, armorY);
                }
            }
        }
    }

    // ─── Binds ───
    private class BindsPart extends HudPart {
        @Override boolean isEnabled() { return bindsEnabled.getValue(); }
        @Override NumberValue posX() { return bindsX; }
        @Override NumberValue posY() { return bindsY; }

        @Override
        void render(DrawContext ctx, BRender b) {
            if (!isEnabled()) return;

            List<Module> bound = new ArrayList<>();
            for (Module m : instance.getModuleManager().getModuleMap().values()) {
                if (m.isEnabled() && m.getKey() != -1) bound.add(m);
            }
            if (bound.isEmpty()) return;

            this.width = 120;
            this.height = 20 + bound.size() * 14;

            drawBg(b, 8);
            b.drawText(bFont, "Binds", x + 8, y + 6, 10f, 0xFFFFFFFF, 2);

            float offset = 22;
            for (Module m : bound) {
                b.drawText(bFont, m.getName(), x + 8, y + offset, 8f, 0xFFFFFFFF, 2);
                String key = cn.remix.util.misc.KeyUtil.getKeyName(m.getKey());
                b.drawText(bFont, key, x + width - 8 - bFont.textSize(key, 8f), y + offset, 8f,
                        getAccentColor(), 2);
                offset += 14;
            }
        }
    }

    // ─── StaffList ───
    private class StaffPart extends HudPart {
        @Override boolean isEnabled() { return staffEnabled.getValue(); }
        @Override NumberValue posX() { return staffX; }
        @Override NumberValue posY() { return staffY; }

        @Override
        void render(DrawContext ctx, BRender b) {
            if (!isEnabled()) return;
            if (mc.getNetworkHandler() == null) return;

            List<String> staff = new ArrayList<>();
            for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
                if (entry.getDisplayName() == null) continue;
                String name = entry.getDisplayName().getString();
                String upper = name.toUpperCase();
                if (upper.contains("ADMIN") || upper.contains("MOD") || upper.contains("DEV")
                        || upper.contains("OWNER") || upper.contains("HELPER")
                        || upper.contains("STAFF")) {
                    staff.add(name);
                }
            }

            if (staff.isEmpty()) return;

            this.width = 140;
            this.height = 24 + staff.size() * 14;

            drawBg(b, 8);
            b.drawText(bFont, "Staff List", x + 8, y + 6, 10f, 0xFFFFFFFF, 2);

            float offset = 22;
            for (String name : staff) {
                b.drawText(bFont, name, x + 10, y + offset, 8f, getAccentColor(), 2);
                offset += 14;
            }
        }
    }

    // ─── Notifications ───
    private class NotifPart extends HudPart {
        @Override boolean isEnabled() { return notifEnabled.getValue(); }
        @Override NumberValue posX() { return notifX; }
        @Override NumberValue posY() { return notifY; }

        @Override
        void render(DrawContext ctx, BRender b) {
            if (!isEnabled()) return;

            // var notifications = Notification.getActiveNotifications();
java.util.List<String> notifications = java.util.Collections.emptyList();
            if (notifications == null || notifications.isEmpty()) return;

            this.width = 160;
            this.height = 24 + notifications.size() * 26;

            drawBg(b, 8);
            b.drawText(bFont, "Notifications", x + 8, y + 6, 10f, 0xFFFFFFFF, 2);

            float offset = 24;
            for (String text : notifications) {
                b.drawText(bFont, text, x + 10, y + offset, 8f, 0xFFFFFFFF, 2);
                offset += 26;
            }
        }
    }

    private static int applyAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private static int darken(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}