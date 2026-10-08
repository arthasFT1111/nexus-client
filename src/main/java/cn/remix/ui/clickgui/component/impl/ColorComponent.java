package cn.remix.ui.clickgui.component.impl;

import cn.remix.module.impl.render.Translator;
import cn.remix.module.value.impl.ColorValue;
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

public final class ColorComponent extends Component {
    private final EasingAnimation visibleAnimation = new EasingAnimation(Easing.EASE_OUT_CUBIC, 200);
    private int dragging = -1;

    public ColorComponent(Module module, ColorValue value) {
        super(module, value);
    }

    @Override
    public float getHeight() {
        visibleAnimation.run(getValue().isVisible() ? 1 : 0);
        return 78 * visibleAnimation.getValue().floatValue();
    }

    @Override
    public void render(DrawContext context, BRender b, BFont bFont,
                       float x, float y, float width, int mouseX, int mouseY, float globalAlpha) {
        super.render(context, b, bFont, x, y, width, mouseX, mouseY, globalAlpha);
        float progress = visibleAnimation.getValue().floatValue();
        this.height = 78 * progress;
        float finalProgress = progress * globalAlpha;
        if (finalProgress < 0.01f) return;

        ColorValue cv = (ColorValue) getValue();
        int alpha = MathHelper.clamp((int) (255 * finalProgress), 0, 255);

        if (bFont != null) {
            b.drawText(bFont, Translator.value(cv.getName()),
                    x + 4, y + 2.0f, 10f, (alpha << 24) | 0xCCCCCC, 20);
        }
        b.roundRect((int) (x + width - 11), (int) (y + 3.5f), 7, 7,
                ColorUtil.applyAlpha(cv.getValue().getRGB(), alpha), 2, 20);

        float satX = x + 4, satY = y + 14, satW = width - 8;
        if (dragging == 0) {
            cv.setHSB(cv.getHue(), MathHelper.clamp((mouseX - satX) / satW, 0, 1),
                    1 - MathHelper.clamp((mouseY - satY) / 50, 0, 1));
        } else if (dragging == 1) {
            cv.setHSB(MathHelper.clamp((mouseX - satX) / satW, 0, 1), cv.getSaturation(), cv.getBrightness());
        }

        // Цветовое поле — через Brapi маленькими прямоугольниками
        for (int i = 0; i < (int) satW; i++) {
            float ratio = i / satW;
            b.roundRect((int) (satX + i), (int) satY, 1, 50,
                    ColorUtil.applyAlpha(Color.HSBtoRGB(cv.getHue(), ratio, 1), alpha), 0, 20);
            b.roundRect((int) (satX + i), (int) (satY + 54), 1, 6,
                    ColorUtil.applyAlpha(Color.HSBtoRGB(ratio, 1, 1), alpha), 0, 20);
        }

        int white = ColorUtil.applyAlpha(Color.WHITE.getRGB(), alpha);
        b.roundRect((int) (satX + cv.getSaturation() * satW - 2), (int) (satY + (1 - cv.getBrightness()) * 50 - 2),
                4, 4, white, 1, 20);
        b.roundRect((int) (satX + cv.getHue() * satW - 1), (int) (satY + 53), 2, 8, white, 1, 20);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (visibleAnimation.getValue().floatValue() < 0.8f || button != 0) return;

        float satX = x + 4, satW = width - 8;
        if (mouseX >= satX && mouseX <= satX + satW) {
            if (mouseY >= y + 14 && mouseY <= y + 64) dragging = 0;
            else if (mouseY >= y + 66 && mouseY <= y + 76) dragging = 1;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = -1;
    }
}