package cn.remix;

import cn.remix.account.AccountManager;
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
import cn.remix.util.render.ShaderEngine;
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
    public static final String name = "Nexus";
    public static final String version = "1.0.0";
    public static Logger logger = LogManager.getLogger(name);

    private EventManager eventManager;
    private ModuleManager moduleManager;
    private CommandManager commandManager;
    private ConfigManager configManager;
    private FontManager fontManager;
    private FriendManager friendManager;
    private TargetManager targetManager;
    private PacketManager packetManager;
    private AccountManager accountManager;

    private CelestialClickGuiScreen clickGuiScreen;

    @Override
    public void onInitializeClient() {
        instance = this;
        init();
    }

    public void init() {
        // Инициализация шейдеров (регистрация RenderPipeline)
        // ShaderEngine.init();

        eventManager = new EventManager();
        fontManager = new FontManager();
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        configManager = new ConfigManager();
        friendManager = new FriendManager();
        targetManager = new TargetManager();
        packetManager = new PacketManager();
        accountManager = new AccountManager();

        clickGuiScreen = new CelestialClickGuiScreen();

        // Регистрация HUD-элемента для HUD-модуля
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of("remix", "hud"),
                (graphics, tickCounter) -> {
                    cn.remix.module.impl.render.HUD hud =
                            instance.getModuleManager().getModule(cn.remix.module.impl.render.HUD.class);
                    if (hud != null && hud.isEnabled()) {
                        hud.renderHud(graphics);
                    }
                }
        );

        logger.info("Nexus initialized.");
    }

    public void shutdown() {
        if (configManager != null) {
            configManager.saveAll();
        }
        if (accountManager != null) {
            accountManager.save();
        }
    }
}