package nl.parrotlync.parrotpluginmanager.bungeecord.task;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import nl.parrotlync.parrotpluginmanager.bungeecord.ParrotPluginManager;
import nl.parrotlync.parrotpluginmanager.common.nexus.NexusClient;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
            String file = plugin.getFile().getName();
            String name = file.split("-")[0];

            if (file.contains("SNAPSHOT")) {
                logger.info(name + " is running on a SNAPSHOT version. These are not updated automatically.");
                continue;
            }

            if (name.equalsIgnoreCase("DiscovChat")) { name = "DiscovChat-Bungee"; }
            String latestVersion = NexusClient.getLatestVersion(name, getAuth(), logger);
            if (latestVersion == null) { continue; }

            if (file.equals(latestVersion)) {
                logger.info(name + " is running the latest release.");
                continue;
            }

            logger.info(name + " is not running the latest release. Starting update...");
            if (NexusClient.downloadFile(name, getAuth(), logger)) {
                hasUpdated = true;
                deletePlugin(plugin);
                logger.info(latestVersion + " has been installed.");
            }
        }

        if (hasUpdated) {
            logger.info("Update check complete! Scheduling server restart to implement new versions...");
            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText("§f[§aInformation§f] §cThe proxy will §lrestart §cin §e1 minute§c."));
            ProxyServer.getInstance().getScheduler().schedule(ParrotPluginManager.getInstance(), () -> ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText("§f[§aInformation§f] §cThe proxy will §lrestart §cin §e30 seconds§c.")), 30, TimeUnit.SECONDS);
            ProxyServer.getInstance().getScheduler().schedule(ParrotPluginManager.getInstance(), () -> ProxyServer.getInstance().stop(), 60, TimeUnit.SECONDS);
        } else {
            logger.info("Update check complete! No new releases have been installed.");
        }
    }

    private String getAuth() {
        return ParrotPluginManager.getInstance().getConfig().getString("nexus-auth-string");
    }

    private void deletePlugin(Plugin plugin) {
        File file = plugin.getFile();
        if (file.delete()) {
            logger.info("Deleted file: " + file.getName());
        } else {
            logger.warning("Couldn't delete file: " + file.getName());
        }
    }
}
