package cn.remix.ui.clickgui.component;

import cn.remix.module.Module;
import cn.remix.module.value.Value;
import cn.remix.util.IMinecraft;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;

import dev.bsprout.brapi.client.BFont;
import dev.bsprout.brapi.client.BRender;

@Getter
public abstract class Component implements IMinecraft {
    protected final Module module;
    private final Value value;
    protected float x, y, width, height;

    public Component(Module module, Value value) {
        this.module = module;
        this.value = value;
    }

    public void render(DrawContext context, BRender b, BFont bFont,
                       float x, float y, float width, int mouseX, int mouseY, float globalAlpha) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {}
    public void mouseReleased(double mouseX, double mouseY, int button) {}

    public float getHeight() { return height; }

    protected boolean hovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}