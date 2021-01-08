package nl.parrotlync.parrotpluginmanager.spigot.task;

import nl.parrotlync.parrotpluginmanager.common.nexus.NexusClient;
import nl.parrotlync.parrotpluginmanager.spigot.ParrotPluginManager;
import nl.parrotlync.parrotpluginmanager.spigot.util.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class UpdateTask implements Runnable {
    private final List<Plugin> plugins;
    private final Logger logger;

    public UpdateTask(List<Plugin> plugins, Logger logger) {
        this.plugins = plugins;
        this.logger = logger;
    }

    @Override
    public void run() {
        boolean hasUpdated = false;
        for (Plugin plugin : plugins) {
            String[] fragments = getFile(plugin).split("/");
            String file = fragments[fragments.length - 1];
            String name = file.split("-")[0];

            if (file.contains("SNAPSHOT")) {
                logger.info(name + " is running on a SNAPSHOT version. These are not updated automatically.");
                continue;
            }

            if (name.equalsIgnoreCase("DiscovChat")) { name = "DiscovChat-Spigot"; }
            String latestVersion = NexusClient.getLatestVersion(name, getAuth(), logger);
            if (latestVersion == null) { continue; }

            if (file.equals(latestVersion)) {
                logger.info(name + " is running the latest release.");
                continue;
            }

            logger.info(name + " is not running the latest release. Starting update...");
            if (NexusClient.downloadFile(name, getAuth(), logger)) {
                hasUpdated = true;
                PluginUtil.unload(name);
                deletePlugin(plugin);
                logger.info(latestVersion + " has been installed.");
            }
        }

        if (hasUpdated) {
            logger.info("Update check complete! Scheduling server restart to implement new versions...");
            Bukkit.broadcastMessage("§f[§aInformation§f] §cThe server you are on will §lrestart §cin §e1 minute§c.");
            Bukkit.getScheduler().runTaskLater(ParrotPluginManager.getInstance(), () -> Bukkit.broadcastMessage("§f[§aInformation§f] §cThe server you are on will §lrestart §cin §e30 seconds§c."), 600);
            Bukkit.getScheduler().runTaskLater(ParrotPluginManager.getInstance(), () -> Bukkit.getServer().shutdown(), 1200);
        } else {
            logger.info("Update check complete! No new releases have been installed.");
        }
    }

    private String getFile(Plugin plugin) {
        String[] fragments = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile().split("/");
        return fragments[fragments.length - 1];
    }

    private String getAuth() {
        return ParrotPluginManager.getInstance().getConfig().getString("nexus-auth-string");
    }

    private void deletePlugin(Plugin plugin) {
        String location = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File file = new File(location);
        if (file.delete()) {
            logger.info("Deleted file: " + file.getName());
        } else {
            logger.warning("Couldn't delete file: " + file.getName());
        }
    }
}
