package cn.remix.ui.screen.impl;

import cn.remix.account.Account;
import cn.remix.ui.screen.AbstractScreen;
import cn.remix.ui.screen.util.AdaptiveButton;
import cn.remix.ui.screen.util.AdaptiveTextBox;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import injection.accessor.MinecraftClientAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.session.Session;
import net.minecraft.util.Identifier;

import dev.bsprout.brapi.client.BFont;
import dev.bsprout.brapi.client.BRender;

import java.util.Optional;
import java.util.UUID;

public class AccountManagerScreen extends AbstractScreen {

    private final Screen parent;
    private AdaptiveTextBox nameBox;
    private AdaptiveTextBox tokenBox;
    private int selectedIndex = -1;

    private static BFont bFont;

    public AccountManagerScreen(Screen parent) {
        super("Account Manager");
        this.parent = parent;
        if (bFont == null) {
            try {
                bFont = new BFont(Identifier.of("brapi", "fonts/noto_sans_regular.ttf"));
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void initScreen() {
        float centerX = this.width / 2f;
        float topY = this.height / 2f - 130;

        nameBox = new AdaptiveTextBox("Nickname (cracked)");
        nameBox.setBounds(centerX - 150, topY, 300, 22);
        textBoxes.add(nameBox);

        tokenBox = new AdaptiveTextBox("Access Token (premium)");
        tokenBox.setBounds(centerX - 150, topY + 28, 300, 22);
        textBoxes.add(tokenBox);

        AdaptiveButton add = new AdaptiveButton("Add Account", this::handleAdd);
        add.setBounds(centerX - 150, topY + 58, 145, 22);
        buttons.add(add);

        AdaptiveButton select = new AdaptiveButton("Switch", this::handleSwitch);
        select.setBounds(centerX + 5, topY + 58, 145, 22);
        buttons.add(select);

        AdaptiveButton remove = new AdaptiveButton("Remove", this::handleRemove);
        remove.setBounds(centerX - 150, topY + 88, 145, 22);
        buttons.add(remove);

        AdaptiveButton back = new AdaptiveButton("Back", () -> mc.setScreen(parent));
        back.setBounds(centerX + 5, topY + 88, 145, 22);
        buttons.add(back);
    }

    private void handleAdd() {
        String name = nameBox.getText().trim();
        String token = tokenBox.getText().trim();

        if (!token.isEmpty()) {
            instance.getAccountManager().add(new Account("premium", token, "premium"));
        } else if (!name.isEmpty()) {
            instance.getAccountManager().add(new Account(name, "", "cracked"));
        }
    }

    private void handleSwitch() {
        var accounts = instance.getAccountManager().getAccounts();
        if (selectedIndex < 0 || selectedIndex >= accounts.size()) return;

        Account acc = accounts.get(selectedIndex);
        if (acc.getType().equals("premium") && !acc.getToken().isEmpty()) {
            loginPremium(acc.getToken());
        } else {
            loginCracked(acc.getName());
        }
        instance.getAccountManager().setCurrent(acc);
    }

    private void loginPremium(String token) {
        new Thread(() -> {
            try {
                var mcAccessor = (MinecraftClientAccessor) mc;
                UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + "premium").getBytes());
                Session session = new Session("premium", uuid, token, Optional.empty(), Optional.empty());
                mcAccessor.setSession(session);
                mcAccessor.setUserApiService(new YggdrasilAuthenticationService(mc.getNetworkProxy()).createUserApiService(token));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loginCracked(String name) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
        Session session = new Session(name, uuid, "", Optional.empty(), Optional.empty());
        ((MinecraftClientAccessor) mc).setSession(session);
    }

    private void handleRemove() {
        var accounts = instance.getAccountManager().getAccounts();
        if (selectedIndex < 0 || selectedIndex >= accounts.size()) return;
        instance.getAccountManager().remove(accounts.get(selectedIndex));
        selectedIndex = -1;
    }

    @Override
    protected void renderScreen(DrawContext context, int mouseX, int mouseY, float delta) {
        // защита от null (пока Client не инициализирован)
        if (instance == null || instance.getFontManager() == null) return;

        // ─── чёрно-фиолетовый фон ───
        float t = (float) ((Math.sin(System.currentTimeMillis() / 3000.0) + 1.0) / 2.0);
        int r1 = (int) (10 + 15 * t);
        int g1 = (int) (5 + 5 * t);
        int b1 = (int) (20 + 25 * t);

        int c1 = (255 << 24) | (r1 << 16) | (g1 << 8) | b1;
        int c2 = (255 << 24) | (5 << 16) | (2 << 8) | 10;

        int steps = 32;
        for (int i = 0; i < steps; i++) {
            float ratio = i / (float) steps;
            int color = interpolate(c2, c1, ratio);
            context.fill(0, (int) (this.height * ratio), this.width,
                    (int) (this.height * (ratio + 1f / steps)) + 1, color);
        }

        // ─── заголовок ───
        var titleFont = instance.getFontManager().getFont(40);
        String title = "Account Manager";
        float tw = titleFont.getStringWidth(title);
        titleFont.drawString(context, title, (this.width - tw) / 2f, this.height / 2f - 180, 0xFFFFFFFF, false);

        // ─── текущий аккаунт ───
        if (bFont != null) {
            Account current = instance.getAccountManager().getCurrent();
            String currentText = current == null ? "Not logged in" :
                    "Current: " + current.getName() + " [" + current.getType() + "]";

            BRender b = new BRender();
            float textW = bFont.textSize(currentText, 10f);
            float cx = (this.width - textW) / 2f;
            float cy = this.height / 2f - 150;
            b.drawText(bFont, currentText, cx, cy, 10f, 0xFFAAAAAA, 1);
            b.flush(context);
        }

        // ─── список аккаунтов ───
        var accounts = instance.getAccountManager().getAccounts();
        float listY = this.height / 2f + 20;
        float listX = this.width / 2f - 150;

        if (bFont != null) {
            BRender b = new BRender();
            for (int i = 0; i < accounts.size(); i++) {
                Account acc = accounts.get(i);
                boolean selected = (i == selectedIndex);
                boolean isCurrent = acc == instance.getAccountManager().getCurrent();

                float y = listY + i * 22;
                int bgColor = selected ? 0xFF3A2A5A : (isCurrent ? 0xFF2A1A3A : 0xFF1A1A25);

                b.roundRect((int) listX, (int) y, 300, 20, bgColor, 6, 0);

                String line = acc.getName() + "  [" + acc.getType() + "]" + (isCurrent ? "  ✔" : "");
                int textColor = selected ? 0xFFFFFFFF : 0xFFCCCCCC;
                b.drawText(bFont, line, listX + 10, y + 5, 10f, textColor, 1);
            }
            b.flush(context);
        }

        for (AdaptiveButton btn : buttons) {
            btn.render(context, mouseX, mouseY, delta);
        }
        for (AdaptiveTextBox box : textBoxes) {
            box.render(context);
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        var accounts = instance.getAccountManager().getAccounts();
        float listY = this.height / 2f + 20;
        float listX = this.width / 2f - 150;

        for (int i = 0; i < accounts.size(); i++) {
            float y = listY + i * 22;
            if (click.x() >= listX && click.x() <= listX + 300
                    && click.y() >= y && click.y() <= y + 20) {
                selectedIndex = i;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
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

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}