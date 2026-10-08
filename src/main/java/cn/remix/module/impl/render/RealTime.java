package cn.remix.module.impl.render;

import cn.remix.event.base.annotation.EventTarget;
import cn.remix.event.impl.Render2DEvent;
import cn.remix.module.Category;
import cn.remix.module.Module;
import cn.remix.module.value.impl.BoolValue;
import cn.remix.module.value.impl.NumberValue;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class RealTime extends Module {

    private final BoolValue showSeconds = new BoolValue("Show Seconds", false);
    private final NumberValue x = new NumberValue("X", 5, 0, 2000);
    private final NumberValue y = new NumberValue("Y", 5, 0, 2000);
    private final BoolValue shadow = new BoolValue("Shadow", true);

    private static final DateTimeFormatter FORMAT_HOURS_MINUTES = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMAT_WITH_SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss");

    public RealTime() {
        super("RealTime", Category.Render);
      
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (mc.player == null || mc.world == null) return;

        LocalTime now = LocalTime.now();
        String text = now.format(showSeconds.getValue()
                ? FORMAT_WITH_SECONDS
                : FORMAT_HOURS_MINUTES);

        int posX = x.getValue().intValue();
        int posY = y.getValue().intValue();
        int color = 0xFFFFFFFF;

        event.getContext().drawText(
                mc.textRenderer,
                text,
                posX,
                posY,
                color,
                shadow.getValue()
        );
    }
}