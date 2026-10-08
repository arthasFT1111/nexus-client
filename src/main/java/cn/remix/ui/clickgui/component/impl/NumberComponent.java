package cn.remix.ui.clickgui.component.impl;

import cn.remix.module.impl.render.Translator;
import cn.remix.module.value.impl.NumberValue;
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

public final class NumberComponent extends Component {
    private final EasingAnimation animation = new EasingAnimation(Easing.EASE_OUT_CUBIC, 150);
    private final EasingAnimation visibleAnimation = new EasingAnimation(Easing.EASE_OUT_CUBIC, 200);
    private boolean dragging;

    public NumberComponent(Module module, NumberValue value) {
        super(module, value);
    }

    @Override
    public float getHeight() {
        visibleAnimation.run(getValue().isVisible() ? 1.0 : 0.0);
        return 20.0f * visibleAnimation.getValue().floatValue();
    }

    @Override
    public void render(DrawContext context, BRender b, BFont bFont,
                       float x, float y, float width, int mouseX, int mouseY, float globalAlpha) {
        super.render(context, b, bFont, x, y, width, mouseX, mouseY, globalAlpha);
        float progress = visibleAnimation.getValue().floatValue();
        this.height = 20.0f * progress;
        float finalProgress = progress * globalAlpha;
        if (finalProgress < 0.01f) return;
        if (dragging) updateValue(mouseX);

        NumberValue nv = (NumberValue) getValue();
        String display = nv.getInc() % 1.0f == 0.0f
                ? String.valueOf(nv.getValue().longValue())
                : String.format("%.2f", nv.getValue());

        int alpha = MathHelper.clamp((int) (255.0f * finalProgress), 0, 255);

        if (bFont != null) {
            b.drawText(bFont, Translator.value(nv.getName()),
                    x + 4.0f, y + 2.0f, 10f, (alpha << 24) | 0xCCCCCC, 20);
            b.drawText(bFont, display,
                    x + width - 6.0f - display.length() * 6f, y + 2.0f, 10f,
                    (alpha << 24) | 0x9A9AAA, 20);
        }

        float barW = width - 10.0f;
        animation.run(MathHelper.clamp((nv.getValue() - nv.getMin()) / (nv.getMax() - nv.getMin()), 0.0f, 1.0f));
        float ratio = animation.getValue().floatValue();

        b.roundRect((int) (x + 4), (int) (y + 14), (int) barW, 2,
                ColorUtil.applyAlpha(new Color(58, 58, 63, alpha).getRGB(), alpha), 1, 20);
        b.roundRect((int) (x + 4), (int) (y + 14), (int) (barW * ratio), 2,
                ColorUtil.applyAlpha(getAccent(), alpha), 1, 20);
        b.roundRect((int) (x + 4 + barW * ratio - 1), (int) (y + 12), 3, 6,
                ColorUtil.applyAlpha(Color.WHITE.getRGB(), alpha), 1, 20);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && visibleAnimation.getValue() > 0.8 && hovered(mouseX, mouseY) && mouseY >= y + 11.0f) {
            dragging = true;
            updateValue(mouseX);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    private void updateValue(double mouseX) {
        NumberValue nv = (NumberValue) getValue();
        float ratio = MathHelper.clamp((float) ((mouseX - (x + 4.0f)) / (width - 10.0f)), 0.0f, 1.0f);
        nv.setValue(Math.round((nv.getMin() + ratio * (nv.getMax() - nv.getMin())) / nv.getInc()) * nv.getInc());
    }

    private int getAccent() {
        try {
            return instance.getModuleManager().getModule(cn.remix.module.impl.render.HUD.class).getColor();
        } catch (Exception e) {
            return new Color(90, 120, 255).getRGB();
        }
    }
}