package nl.parrotlync.parrotpluginmanager;

import nl.parrotlync.parrotpluginmanager.command.PPMCommandExecutor;
import nl.parrotlync.parrotpluginmanager.task.UpdateCheck;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParrotPluginManager extends JavaPlugin {
    private static ParrotPluginManager instance;
    private final List<Plugin> plugins = new ArrayList<>();

    public ParrotPluginManager() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        PluginManager pluginManager = getServer().getPluginManager();
        if (getConfig().getString("nexus-auth-string").isEmpty()) {
            getLogger().warning("Please enter the nexus-auth-string in the config to use ParrotPluginManager!");
            pluginManager.disablePlugin(this);
            return;
        }
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (plugin.getDescription().getAuthors().contains("ParrotLync") && !plugin.getName().equals("ParrotPluginManager")) {
                plugins.add(plugin);
            }
        }
        getServer().getScheduler().runTaskAsynchronously(this, new UpdateCheck(plugins));
        getCommand("ppm").setExecutor(new PPMCommandExecutor());
        getLogger().info("ParrotPluginManager is now enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ParrotPluginManager is now disabled!");
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }

    public static ParrotPluginManager getInstance() {
        return instance;
    }
}
