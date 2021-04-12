package nl.parrotlync.parrotpluginmanager.spigot.command;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.parrotlync.parrotpluginmanager.common.nexus.NexusClient;
import nl.parrotlync.parrotpluginmanager.spigot.ParrotPluginManager;
import nl.parrotlync.parrotpluginmanager.spigot.task.UpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class PPMCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("ppm.check") && args.length > 0) {
            if (args[0].equalsIgnoreCase("check")) {
                Bukkit.getScheduler().runTaskAsynchronously(ParrotPluginManager.getInstance(),
                        new UpdateTask(ParrotPluginManager.getInstance().getPlugins(), ParrotPluginManager.getInstance().getLogger(), false));
                sender.sendMessage("§7Update check has started [SNAPSHOTS = false]. Please refer to the logs to see the results.");
                return true;
            }

            if (args[0].equalsIgnoreCase("forceSnapshots")) {
                Bukkit.getScheduler().runTaskAsynchronously(ParrotPluginManager.getInstance(),
                        new UpdateTask(ParrotPluginManager.getInstance().getPlugins(), ParrotPluginManager.getInstance().getLogger(), true));
                sender.sendMessage("§7Update check has started [SNAPSHOTS = true]. Please refer to the logs to see the results.");
                return true;
            }

            if (args[0].equalsIgnoreCase("version")) {
                if (args.length == 2) {
                    Plugin plugin = Bukkit.getPluginManager().getPlugin(args[1]);
                    if (plugin != null) {
                        Bukkit.getScheduler().runTaskAsynchronously(ParrotPluginManager.getInstance(), () -> {
                            String[] fragments = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile().split("/");
                            String file = fragments[fragments.length - 1];
                            String currentVersion = file.replace(plugin.getName() + "-", "").replace(".jar", "");
                            if (file.contains("SNAPSHOT")) {
                                sender.sendMessage("§3" + plugin.getName() + " §7is running a SNAPSHOT version. (" + currentVersion + ")");
                            } else {
                                String name = file.split("-")[0];
                                if (name.equalsIgnoreCase("DiscovChat")) { name = "DiscovChat-Spigot"; }
                                String latest = NexusClient.getLatestVersion(name,
                                        ParrotPluginManager.getInstance().getConfig().getString("nexus-auth-string"),
                                        ParrotPluginManager.getInstance().getLogger());
                                if (file.equalsIgnoreCase(latest)) {
                                    sender.sendMessage("§3" + plugin.getName() + " §7is running the §alatest §7version. (" + currentVersion + ")");
                                } else if (latest == null) {
                                    sender.sendMessage("§3" + plugin.getName() + " §7is not available at Nexus. We're not able to check for updates.");
                                } else {
                                    sender.sendMessage("§7There is a new version available for §3" + plugin.getName() + "§7 (" + latest + "). It is currently running " + currentVersion);
                                }
                            }
                        });
                        return true;
                    } else {
                        sender.sendMessage("§cThat is not a valid plugin!");
                    }
                } else {
                    sender.sendMessage("§cYou need to specify a plugin!");
                }
                return true;
            }
        }

        return help(sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (sender.hasPermission("ppm.check")) {
            if (args.length == 1) {
                suggestions.add("check");
                suggestions.add("version");
                suggestions.add("forceSnapshots");
                return StringUtil.copyPartialMatches(args[0], suggestions, new ArrayList<>());
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("version")) {
                for (Plugin plugin : ParrotPluginManager.getInstance().getPlugins()) {
                    suggestions.add(plugin.getName());
                }
                return StringUtil.copyPartialMatches(args[1], suggestions, new ArrayList<>());
            }
        }

        return suggestions;
    }

    private boolean help(CommandSender sender) {
        if (sender.hasPermission("ppm.check")) {
            sender.sendMessage("§f+---+ §bParrotPluginManager §f+---+");
            sender.spigot().sendMessage(getTextComponent("§3/ppm check",
                    "§7Checks and updates all loaded plugins by ParrotLync.",
                    "/ppm check"));
            sender.spigot().sendMessage(getTextComponent("§3/ppm version <plugin>",
                    "§7Checks which version a plugin is currently running.",
                    "/ppm version "));
            sender.spigot().sendMessage(getTextComponent("§3/ppm forceSnapshots",
                    "§7Force update all snapshot plugins.",
                    "/ppm forceSnapshots "));
        } else {
            sender.sendMessage("§cYou don't have permission to do that!");
        }
        return true;
    }

    private TextComponent getTextComponent(String message, String hoverMessage, String command) {
        TextComponent main = new TextComponent(message);
        main.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverMessage).create()));
        main.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        return main;
    }
}
