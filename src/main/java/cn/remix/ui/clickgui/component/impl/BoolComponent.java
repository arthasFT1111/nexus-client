package cn.remix.ui.clickgui.component.impl;

import cn.remix.module.impl.render.Translator;
import cn.remix.module.value.impl.BoolValue;
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

public final class BoolComponent extends Component {
    private final EasingAnimation animation = new EasingAnimation(Easing.EASE_OUT_CUBIC, 150);
    private final EasingAnimation visibleAnimation = new EasingAnimation(Easing.EASE_OUT_CUBIC, 200);

    public BoolComponent(Module module, BoolValue value) {
        super(module, value);
    }

    @Override
    public float getHeight() {
        visibleAnimation.run(getValue().isVisible() ? 1 : 0);
        return 14 * visibleAnimation.getValue().floatValue();
    }

    @Override
    public void render(DrawContext context, BRender b, BFont bFont,
                       float x, float y, float width, int mouseX, int mouseY, float globalAlpha) {
        super.render(context, b, bFont, x, y, width, mouseX, mouseY, globalAlpha);
        float progress = visibleAnimation.getValue().floatValue();
        this.height = 14 * progress;
        float finalProgress = progress * globalAlpha;
        if (finalProgress < 0.01f) return;

        BoolValue bv = (BoolValue) getValue();
        int alpha = MathHelper.clamp((int) (255 * finalProgress), 0, 255);

        if (bFont != null) {
            b.drawText(bFont, Translator.value(bv.getName()),
                    x + 4, y + (14 - 10) / 2.0f + 0.5f, 10f,
                    (alpha << 24) | 0xCCCCCC, 20);
        }

        animation.run(bv.getValue() ? 1 : 0);
        int targetColor = ColorUtil.interpolate(
                new Color(58, 58, 63, alpha).getRGB(),
                getAccent(),
                animation.getValue().floatValue());

        b.roundRect((int) (x + width - 11), (int) (y + 3.5f), 7, 7,
                ColorUtil.applyAlpha(targetColor, alpha), 2, 20);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hovered(mouseX, mouseY) && visibleAnimation.getValue().floatValue() > 0.8f)
            ((BoolValue) getValue()).toggle();
    }

    private int getAccent() {
        try {
            return instance.getModuleManager().getModule(cn.remix.module.impl.render.HUD.class).getColor();
        } catch (Exception e) {
            return new Color(90, 120, 255).getRGB();
        }
    }
}