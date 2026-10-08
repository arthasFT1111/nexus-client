package cn.remix.ui.screen.impl;

import cn.remix.ui.screen.AbstractScreen;
import cn.remix.ui.screen.util.AdaptiveButton;
import dev.bsprout.brapi.client.BRender;
import dev.bsprout.brapi.client.BTexture;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class MainMenu extends AbstractScreen {

    private BTexture logoTexture;

    public MainMenu() {
        super("Main Menu");
    }

    @Override
    protected void initScreen() {
        if (logoTexture == null) {
            try {
                logoTexture = new BTexture(Identifier.of("remix", "textures/gui/logo.png"));
            } catch (Exception e) {
                logoTexture = null;
            }
        }

        float centerX = this.width / 2f;
        float centerY = this.height / 2f;

        AdaptiveButton singleplayer = new AdaptiveButton("Singleplayer",
                () -> mc.setScreen(new net.minecraft.client.gui.screen.world.SelectWorldScreen(this)));
        singleplayer.setBounds(centerX - 100, centerY - 40, 200, 24);
        buttons.add(singleplayer);

        AdaptiveButton multiplayer = new AdaptiveButton("Multiplayer",
                () -> mc.setScreen(new net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen(this)));
        multiplayer.setBounds(centerX - 100, centerY - 10, 200, 24);
        buttons.add(multiplayer);

        AdaptiveButton options = new AdaptiveButton("Options",
                () -> mc.setScreen(new net.minecraft.client.gui.screen.option.OptionsScreen(this, mc.options)));
        options.setBounds(centerX - 100, centerY + 20, 200, 24);
        buttons.add(options);

        AdaptiveButton accountManager = new AdaptiveButton("Account Manager",
                () -> mc.setScreen(new AccountManagerScreen(this)));
        accountManager.setBounds(centerX - 100, centerY + 50, 200, 24);
        buttons.add(accountManager);

        AdaptiveButton exit = new AdaptiveButton("Exit", () -> mc.scheduleStop());
        exit.setBounds(centerX - 100, centerY + 80, 200, 24);
        buttons.add(exit);
    }

    @Override
    protected void renderScreen(DrawContext context, int mouseX, int mouseY, float delta) {
        // ─── градиент (всегда) ───
        float t = (float) ((Math.sin(System.currentTimeMillis() / 3000.0) + 1.0) / 2.0);

        int r1 = (int) (10 + 15 * t);
        int g1 = (int) (5 + 5 * t);
        int b1 = (int) (20 + 25 * t);

        int r2 = 5;
        int g2 = 2;
        int b2 = 10;

        int c1 = (255 << 24) | (r1 << 16) | (g1 << 8) | b1;
        int c2 = (255 << 24) | (r2 << 16) | (g2 << 8) | b2;

        int steps = 32;
        for (int i = 0; i < steps; i++) {
            float ratio = i / (float) steps;
            int color = interpolate(c2, c1, ratio);
            int y1 = (int) (this.height * ratio);
            int y2 = (int) (this.height * (ratio + 1f / steps)) + 1;
            context.fill(0, y1, this.width, y2, color);
        }

        // ─── дальше — только если Client готов ───
        if (instance == null || instance.getFontManager() == null) return;

        // лого Nexus
        if (logoTexture != null) {
            BRender b = new BRender();
            float logoSize = 96;
            float logoX = (this.width - logoSize) / 2f;
            float logoY = this.height / 2f - 220;
            b.drawTexture(logoTexture, logoX, logoY, logoSize, logoSize, 0xFFFFFFFF, true, 0);
            b.flush(context);
        }

        // заголовок
        var font = instance.getFontManager().getFont(48);
        String title = "Nexus";
        float titleWidth = font.getStringWidth(title);
        font.drawString(context, title, (this.width - titleWidth) / 2f, this.height / 2f - 110, 0xFFFFFFFF, false);

        // кнопки
        for (AdaptiveButton btn : buttons) {
            btn.render(context, mouseX, mouseY, delta);
        }
    }

    private static int interpolate(int c1, int c2, float ratio) {
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}