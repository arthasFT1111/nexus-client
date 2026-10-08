package cn.remix.util.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;

public class ShaderEngine {

    public static final RenderPipeline NEXUS_GRADIENT = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
                    .withLocation(Identifier.of("remix", "pipeline/nexus_gradient"))
                    .withVertexShader("core/nexus_gradient")
                    .withFragmentShader("core/nexus_gradient")
                    .build()
    );

    public static void init() {
        // Просто загружаем класс, чтобы статический инициализатор сработал
    }
}