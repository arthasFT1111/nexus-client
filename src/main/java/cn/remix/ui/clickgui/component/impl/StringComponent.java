package cn.remix.ui.clickgui.component.impl;

import cn.remix.module.impl.render.Translator;
import cn.remix.module.value.impl.StringValue;
import cn.remix.module.Module;
import cn.remix.ui.clickgui.component.Component;
import cn.remix.util.animation.Easing;
import cn.remix.util.animation.EasingAnimation;
import cn.remix.util.render.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import dev.bsprout.brapi.client.BFont;
import dev.bsprout.brapi.client.BRender;

import java.awt.*;

public final class StringComponent extends Component {
    private static final int MAX_LENGTH = 64;

    private final EasingAnimation visibleAnimation = new EasingAnimation(Easing.EASE_OUT_CUBIC, 200);
    private boolean focused;

    public StringComponent(Module module, StringValue value) {
        super(module, value);
    }

    @Override
    public float getHeight() {
        visibleAnimation.run(getValue().isVisible() ? 1.0 : 0.0);
        return 16.0f * visibleAnimation.getValue().floatValue();
    }

    @Override
    public void render(DrawContext context, BRender b, BFont bFont,
                       float x, float y, float width, int mouseX, int mouseY, float globalAlpha) {
        super.render(context, b, bFont, x, y, width, mouseX, mouseY, globalAlpha);
        float progress = visibleAnimation.getValue().floatValue();
        this.height = 16.0f * progress;
        float finalProgress = progress * globalAlpha;
        if (finalProgress < 0.01f) return;

        StringValue sv = (StringValue) getValue();
        int alpha = MathHelper.clamp((int) (255.0f * finalProgress), 0, 255);

        String label = Translator.value(sv.getName());
        String value = sv.getValue();

        if (bFont != null) {
            b.drawText(bFont, label, x + 4.0f, y + (16 - 10) / 2.0f + 0.5f, 10f,
                    (alpha << 24) | 0xCCCCCC, 20);

            int valueColor = focused ? getAccent() : (new Color(170, 170, 170).getRGB());
            b.drawText(bFont, value, x + 8.0f + label.length() * 5.5f,
                    y + (16 - 10) / 2.0f + 0.5f, 10f,
                    ColorUtil.applyAlpha(valueColor, alpha), 20);

            if (focused) {
                float cursorX = x + 8.0f + label.length() * 5.5f + value.length() * 5.5f;
                b.rect((int) cursorX, (int) (y + 3.0f), 1, 10,
                        ColorUtil.applyAlpha(getAccent(), alpha), 20);
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (visibleAnimation.getValue() < 0.8f || button != 0) return;
        focused = hovered(mouseX, mouseY);
    }

    public boolean keyTyped(int keyCode) {
        if (!focused) return false;

        StringValue sv = (StringValue) getValue();
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            String value = sv.getValue();
            if (!value.isEmpty()) {
                sv.setValue(value.substring(0, value.length() - 1));
            }
        } else if (keyCode == GLFW.GLFW_KEY_ENTER
                || keyCode == GLFW.GLFW_KEY_KP_ENTER
                || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            focused = false;
        }
        return true;
    }

    public boolean charTyped(CharInput input) {
        if (!focused || !input.isValidChar()) return false;

        StringValue sv = (StringValue) getValue();
        if (sv.getValue().length() < MAX_LENGTH) {
            sv.setValue(sv.getValue() + input.asString());
        }
        return true;
    }

    private int getAccent() {
        try {
            return instance.getModuleManager().getModule(cn.remix.module.impl.render.HUD.class).getColor();
        } catch (Exception e) {
            return new Color(90, 120, 255).getRGB();
        }
    }
}