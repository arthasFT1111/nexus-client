package cn.remix.config;

import cn.remix.Client;
import cn.remix.config.impl.ModuleConfig;
import cn.remix.util.IMinecraft;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ConfigManager implements IMinecraft {
    @Getter
    private final List<Config> configs = new ArrayList<>();

    public ConfigManager() {
        instance.getEventManager().register(this);

        addConfigs(
                new ModuleConfig()
        );

        loadAll();
        scanConfigs();
    }

    public Config getConfig(final String name) {
        return this.configs.stream()
                .filter(config -> config.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public List<String> getAvailableConfigs() {
        List<String> configNames = new ArrayList<>();
        File directory = new File(Client.name, "configs");
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    configNames.add(name.substring(0, name.length() - 5));
                }
            }
        }

        if (configNames.isEmpty()) configNames.add("Default");
        return configNames;
    }

    public void addConfigs(final Config... configsArray) {
        configs.addAll(Arrays.asList(configsArray));
    }

    public void saveAll() {
        configs.forEach(Config::save);
    }

    public void loadAll() {
        configs.forEach(Config::load);
    }

    public void scanConfigs() {
        File directory = new File(Client.name, "configs");
        if (!directory.exists() || !directory.isDirectory()) return;

        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            String name = file.getName();
            name = name.substring(0, name.length() - 5);

            if (getConfig(name) == null) {
                configs.add(new ModuleConfig(name));
            }
        }
    }

    public void saveConfig(String name) {
        Config config = getConfig(name);
        if (config != null) {
            config.save();
            Client.logger.info("Saved config: {}", name);
        } else {
            ModuleConfig newConfig = new ModuleConfig(name);
            newConfig.save();
            configs.add(newConfig);
            Client.logger.info("Created and saved config: {}", name);
        }
    }

    public void loadConfig(String name) {
        Config config = getConfig(name);
        if (config != null) {
            config.load();
            Client.logger.info("Loaded config: {}", name);
        } else {
            Client.logger.warn("Config not found: {}", name);
        }
    }

    public void createConfig(String name) {
        if (getConfig(name) != null) return;
        ModuleConfig config = new ModuleConfig(name);
        configs.add(config);
        config.save();
    }

    public void deleteConfig(String name) {
        Config config = getConfig(name);
        if (config != null) {
            File file = config.getFile();
            if (file.exists()) {
                file.delete();
            }
            configs.remove(config);
            Client.logger.info("Deleted config: {}", name);
        }
    }
}