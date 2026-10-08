package cn.remix.ui.clickgui.component.impl;

import cn.remix.module.impl.render.Translator;
import cn.remix.module.value.impl.ModeValue;
import cn.remix.module.Module;
import cn.remix.ui.clickgui.component.Component;
import cn.remix.util.animation.Easing;
import cn.remix.util.animation.EasingAnimation;
import cn.remix.util.render.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import dev.bsprout.brapi.client.BFont;
import dev.bsprout.brapi.client.BRender;

import java.awt.*;

public final class ModeComponent extends Component {
    private final EasingAnimation visibleAnimation = new EasingAnimation(Easing.EASE_OUT_CUBIC, 200);

    public ModeComponent(Module module, ModeValue value) {
        super(module, value);
    }

    @Override
    public float getHeight() {
        visibleAnimation.run(getValue().isVisible() ? 1.0 : 0.0);
        return 14.0f * visibleAnimation.getValue().floatValue();
    }

    @Override
    public void render(DrawContext context, BRender b, BFont bFont,
                       float x, float y, float width, int mouseX, int mouseY, float globalAlpha) {
        super.render(context, b, bFont, x, y, width, mouseX, mouseY, globalAlpha);
        float progress = visibleAnimation.getValue().floatValue();
        this.height = 14.0f * progress;
        float finalProgress = progress * globalAlpha;
        if (finalProgress < 0.01f) return;

        ModeValue mv = (ModeValue) getValue();
        int alpha = MathHelper.clamp((int) (255.0f * finalProgress), 0, 255);

        if (bFont != null) {
            b.drawText(bFont, Translator.value(mv.getName()),
                    x + 4.0f, y + (14 - 10) / 2.0f + 0.5f, 10f,
                    (alpha << 24) | 0xCCCCCC, 20);
            b.drawText(bFont, mv.getValue(),
                    x + width - 4.0f - mv.getValue().length() * 6f,
                    y + (14 - 10) / 2.0f + 0.5f, 10f,
                    ColorUtil.applyAlpha(getAccent(), alpha), 20);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (visibleAnimation.getValue() < 0.8f || !hovered(mouseX, mouseY) || (button != 0 && button != 1)) return;
        ModeValue mv = (ModeValue) getValue();
        String[] modes = mv.getModes();
        int index = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(mv.getValue())) { index = i; break; }
        }
        mv.setValue(modes[(index + (button == 0 ? 1 : -1) + modes.length) % modes.length]);
    }

    private int getAccent() {
        try {
            return instance.getModuleManager().getModule(cn.remix.module.impl.render.HUD.class).getColor();
        } catch (Exception e) {
            return new Color(90, 120, 255).getRGB();
        }
    }
}