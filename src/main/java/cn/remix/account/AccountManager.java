package cn.remix.account;

import cn.remix.Client;
import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private final List<Account> accounts = new ArrayList<>();
    private Account current;
    private final File file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public AccountManager() {
        File dir = new File("Nexus", "accounts");
        if (!dir.exists()) dir.mkdirs();
        this.file = new File(dir, "accounts.json");
        load();
    }

    public List<Account> getAccounts() { return accounts; }
    public Account getCurrent() { return current; }

    public void add(Account account) {
        accounts.add(account);
        save();
    }

    public void remove(Account account) {
        accounts.remove(account);
        if (current == account) current = null;
        save();
    }

    public void setCurrent(Account account) {
        this.current = account;
        save();
    }

    public void save() {
        try {
            JsonArray array = new JsonArray();
            for (Account acc : accounts) {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", acc.getName());
                obj.addProperty("token", acc.getToken());
                obj.addProperty("type", acc.getType());
                array.add(obj);
            }
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println(gson.toJson(array));
            }
        } catch (Exception e) {
            Client.logger.error("Failed to save accounts: " + e.getMessage());
        }
    }

    public void load() {
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            JsonElement el = JsonParser.parseReader(reader);
            if (!el.isJsonArray()) return;
            for (JsonElement e : el.getAsJsonArray()) {
                JsonObject obj = e.getAsJsonObject();
                Account acc = new Account(
                        obj.get("name").getAsString(),
                        obj.get("token").getAsString(),
                        obj.get("type").getAsString()
                );
                accounts.add(acc);
            }
        } catch (Exception e) {
            Client.logger.error("Failed to load accounts: " + e.getMessage());
        }
    }
}