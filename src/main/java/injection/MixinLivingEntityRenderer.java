package injection;

import cn.remix.module.impl.render.TargetOutline;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer {

    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void onUpdateRenderState(LivingEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
        TargetOutline outline = TargetOutline.getInstance();
        if (outline == null || !outline.isEnabled()) return;

        LivingEntity target = outline.getCurrentTarget();
        if (target == null || target != entity) return;

        // устанавливаем цвет контура
        state.outlineColor = outline.getOutlineColor();
    }
}