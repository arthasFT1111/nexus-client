package cn.remix;

import cn.remix.command.CommandManager;
import cn.remix.config.ConfigManager;
import cn.remix.event.base.EventManager;
import cn.remix.management.FriendManager;
import cn.remix.management.PacketManager;
import cn.remix.management.TargetManager;
import cn.remix.module.ModuleManager;
import cn.remix.ui.clickgui.CelestialClickGuiScreen;
import cn.remix.ui.font.FontManager;
import cn.remix.util.IMinecraft;
import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class Client implements ClientModInitializer, IMinecraft {

    public static Client instance;
    public static final String name = "Remix";
    public static final String version = "2.4.3";
    public static Logger logger = LogManager.getLogger(name);

    private EventManager eventManager;
    private ModuleManager moduleManager;
    private CommandManager commandManager;
    private ConfigManager configManager;
    private FontManager fontManager;
    private FriendManager friendManager;
    private TargetManager targetManager;
    private PacketManager packetManager;

    private CelestialClickGuiScreen clickGuiScreen;

    @Override
    public void onInitializeClient() {
        instance = this;
        init();
    }

    public void init() {
        eventManager = new EventManager();
        fontManager = new FontManager();
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        configManager = new ConfigManager();
        friendManager = new FriendManager();
        targetManager = new TargetManager();
        packetManager = new PacketManager();

        clickGuiScreen = new CelestialClickGuiScreen();

        // ─── Регистрация HUD-элемента для Watermark ───
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of("remix", "watermark"),
                (graphics, tickCounter) -> {
                    cn.remix.module.impl.render.Watermark wm =
                            instance.getModuleManager().getModule(cn.remix.module.impl.render.Watermark.class);
                    if (wm != null && wm.isEnabled()) {
                        wm.renderWatermark(graphics);
                    }
                }
        );

        logger.info("Remix initialized.");
    }

    public void shutdown() {
        if (configManager != null) {
            configManager.saveAll();
        }
    }
}