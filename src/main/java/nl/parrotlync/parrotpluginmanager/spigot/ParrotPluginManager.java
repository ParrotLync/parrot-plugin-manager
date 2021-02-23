package nl.parrotlync.parrotpluginmanager.spigot;

import nl.parrotlync.parrotpluginmanager.spigot.command.PPMCommand;
import nl.parrotlync.parrotpluginmanager.spigot.task.UpdateTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ParrotPluginManager extends JavaPlugin {
    private static ParrotPluginManager instance;

    public ParrotPluginManager() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (getConfig().getString("nexus-auth-string").isEmpty()) {
            getLogger().warning("Please enter the nexus-auth-string in the config to use ParrotPluginManager!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("ppm").setExecutor(new PPMCommand());
        getLogger().info("ParrotPluginManager is now enabled!");
    }

    @Override
    public void onDisable() {
        List<Plugin> plugins = getPlugins();
        new UpdateTask(plugins, getLogger()).run();
        getLogger().info("ParrotPluginManager is now disabled!");
    }

    public List<Plugin> getPlugins() {
        PluginManager pluginManager = getServer().getPluginManager();
        List<Plugin> plugins = new ArrayList<>();
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (plugin.getDescription().getAuthors().contains("ParrotLync") && !plugin.getName().equals("ParrotPluginManager")) {
                plugins.add(plugin);
            }
        }
        return plugins;
    }

    public static ParrotPluginManager getInstance() {
        return instance;
    }
}
