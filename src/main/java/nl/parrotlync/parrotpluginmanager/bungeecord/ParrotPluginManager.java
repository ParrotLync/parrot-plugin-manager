package nl.parrotlync.parrotpluginmanager.bungeecord;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import nl.parrotlync.parrotpluginmanager.bungeecord.command.BPPMCommand;
import nl.parrotlync.parrotpluginmanager.bungeecord.task.UpdateTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ParrotPluginManager extends Plugin {
    private static ParrotPluginManager instance;

    public ParrotPluginManager() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (getConfig().getString("nexus-auth-string").isEmpty()) {
            getLogger().warning("Please enter the nexus-auth-string in the config to use ParrotPluginManager!");
            return;
        }

        getProxy().getPluginManager().registerCommand(this, new BPPMCommand());
    }

    @Override
    public void onDisable() {
        List<Plugin> plugins = getPlugins();
        new UpdateTask(plugins, getLogger()).run();
    }

    public List<Plugin> getPlugins() {
        PluginManager pluginManager = getProxy().getPluginManager();
        List<Plugin> plugins = new ArrayList<>();
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (plugin.getFile().getName().contains("ParrotPluginManager")) { continue; }
            if (plugin.getDescription().getAuthor().contains("ParrotLync")) {
                plugins.add(plugin);
            }
        }
        return plugins;
    }

    private void saveDefaultConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {

            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Configuration getConfig() {
        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Configuration();
    }

    public static ParrotPluginManager getInstance() {
        return instance;
    }
}
