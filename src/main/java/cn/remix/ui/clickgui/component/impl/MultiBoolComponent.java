package cn.remix.ui.clickgui.component.impl;

import cn.remix.module.impl.render.Translator;
import cn.remix.module.value.impl.BoolValue;
import cn.remix.module.value.impl.MultiBoolValue;
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
import java.util.List;

public final class MultiBoolComponent extends Component {
    private final EasingAnimation[] animations;
    private final EasingAnimation visibleAnimation = new EasingAnimation(Easing.EASE_OUT_CUBIC, 200);

    public MultiBoolComponent(Module module, MultiBoolValue value) {
        super(module, value);
        List<BoolValue> subValues = value.getValues();
        this.animations = new EasingAnimation[subValues.size()];
        for (int i = 0; i < animations.length; i++)
            this.animations[i] = new EasingAnimation(Easing.EASE_OUT_CUBIC, 150);
    }

    @Override
    public float getHeight() {
        visibleAnimation.run(getValue().isVisible() ? 1.0 : 0.0);
        return (18 + (((MultiBoolValue) getValue()).getValues().size() * 14.0f)) * visibleAnimation.getValue().floatValue();
    }

    @Override
    public void render(DrawContext context, BRender b, BFont bFont,
                       float x, float y, float width, int mouseX, int mouseY, float globalAlpha) {
        super.render(context, b, bFont, x, y, width, mouseX, mouseY, globalAlpha);
        float progress = visibleAnimation.getValue().floatValue();
        List<BoolValue> subValues = ((MultiBoolValue) getValue()).getValues();
        this.height = (18 + (subValues.size() * 14.0f)) * progress;
        float finalProgress = progress * globalAlpha;
        if (finalProgress < 0.01f) return;

        int alpha = MathHelper.clamp((int) (255.0f * finalProgress), 0, 255);
        String title = " - " + Translator.value(getValue().getName()) + " - ";
        if (bFont != null) {
            b.drawText(bFont, title, x + 4, y + 2.0f, 10f, (alpha << 24) | 0xCCCCCC, 20);
        }

        float offset = 16.0f;
        for (int i = 0; i < subValues.size(); i++) {
            BoolValue bool = subValues.get(i);
            if (bFont != null) {
                b.drawText(bFont, Translator.value(bool.getName()),
                        x + 4.0f, y + offset + (14 - 10) / 2.0f + 0.5f, 10f,
                        (alpha << 24) | 0xAAAAAA, 20);
            }
            animations[i].run(bool.getValue() ? 1.0 : 0.0);
            int targetColor = ColorUtil.interpolate(
                    new Color(58, 58, 63, alpha).getRGB(),
                    getAccent(),
                    animations[i].getValue().floatValue());
            b.roundRect((int) (x + width - 11.0f), (int) (y + offset + 3.5f), 7, 7,
                    ColorUtil.applyAlpha(targetColor, alpha), 2, 20);
            offset += 14.0f;
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || visibleAnimation.getValue() < 0.8) return;
        float offset = 16.0f;
        for (BoolValue bool : ((MultiBoolValue) getValue()).getValues()) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y + offset && mouseY <= y + offset + 14.0f)
                bool.toggle();
            offset += 14.0f;
        }
    }

    private int getAccent() {
        try {
            return instance.getModuleManager().getModule(cn.remix.module.impl.render.HUD.class).getColor();
        } catch (Exception e) {
            return new Color(90, 120, 255).getRGB();
        }
    }
}