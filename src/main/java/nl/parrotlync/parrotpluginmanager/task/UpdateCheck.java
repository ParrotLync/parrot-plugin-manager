package nl.parrotlync.parrotpluginmanager.task;

import nl.parrotlync.parrotpluginmanager.ParrotPluginManager;
import nl.parrotlync.parrotpluginmanager.util.NexusClient;
import nl.parrotlync.parrotpluginmanager.util.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;

public class UpdateCheck implements Runnable {
    private final List<Plugin> plugins;

    public UpdateCheck(List<Plugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    public void run() {
        for (Plugin plugin : plugins) {
            String[] fragments = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile().split("/");
            String file = fragments[fragments.length - 1];
            String name = file.split("-")[0];
            if (name.equalsIgnoreCase("DiscovChat")) { name = "DiscovChat-Spigot"; }
            if (!file.contains("SNAPSHOT")) {
                String latest = NexusClient.getLatestVersion(name);
                if (latest != null) {
                    if (!file.equals(latest)) {
                        info(plugin.getName() + " is not running the latest release. Starting update...");
                        if (NexusClient.downloadFile(name)) {
                            PluginUtil.unload(plugin);
                            deletePlugin(plugin);
                            if (PluginUtil.load(latest)) {
                                info(latest + " has been installed and loaded.");
                            }
                        }
                    } else {
                        info(plugin.getName() + " is running the latest release.");
                    }
                }
            } else {
                info(plugin.getName() + " is running on a SNAPSHOT version. Snapshots are not updated automatically.");
            }
        }
        info("Update check complete!");
    }

    private void deletePlugin(Plugin plugin) {
        String location = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File file = new File(location);
        if (file.delete()) {
            info("Deleted file: " + file.getName());
        } else {
            warn("Couldn't delete file: " + file.getName());
        }
    }

    private void info(String message) {
        ParrotPluginManager.getInstance().getLogger().info(message);
    }

    private void warn(String message) {
        ParrotPluginManager.getInstance().getLogger().warning(message);
    }
}
