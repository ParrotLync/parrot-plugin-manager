package nl.parrotlync.parrotpluginmanager.bungeecord.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import nl.parrotlync.parrotpluginmanager.bungeecord.ParrotPluginManager;
import nl.parrotlync.parrotpluginmanager.bungeecord.task.UpdateTask;
import nl.parrotlync.parrotpluginmanager.common.nexus.NexusClient;

public class BPPMCommand extends Command {

    public BPPMCommand() {
        super("bppm");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("ppm.check") && args.length > 0) {
            if (args[0].equalsIgnoreCase("check")) {
                ProxyServer.getInstance().getScheduler().runAsync(ParrotPluginManager.getInstance(),
                        new UpdateTask(ParrotPluginManager.getInstance().getPlugins(), ParrotPluginManager.getInstance().getLogger()));
                sender.sendMessage(TextComponent.fromLegacyText("§7Update check has started. Please refer to the logs to see the results."));
                return;
            }

            if (args[0].equalsIgnoreCase("version")) {
                if (args.length == 2) {
                    Plugin plugin = ProxyServer.getInstance().getPluginManager().getPlugin(args[1]);
                    if (plugin != null) {
                        ProxyServer.getInstance().getScheduler().runAsync(ParrotPluginManager.getInstance(), () -> {
                            String file = plugin.getFile().getName();
                            String name = file.split("-")[0];
                            String currentVersion = file.replace(name + "-", "").replace(".jar", "");
                            if (file.contains("SNAPSHOT")) {
                                sender.sendMessage(TextComponent.fromLegacyText("§3" + name + " §7is running a SNAPSHOT §7version. (" + currentVersion + ")"));
                            } else {
                                String latest = NexusClient.getLatestVersion(name,
                                        ParrotPluginManager.getInstance().getConfig().getString("nexus-auth-string"),
                                        ParrotPluginManager.getInstance().getLogger());
                                if (file.equalsIgnoreCase(latest)) {
                                    sender.sendMessage(TextComponent.fromLegacyText("§3" + name + " §7is running the §alatest §7version. (" + currentVersion + ")"));
                                } else if (latest == null) {
                                    sender.sendMessage(TextComponent.fromLegacyText("§3" + name + " §7is not available at Nexus. We're not able to check for updates."));
                                } else {
                                    sender.sendMessage(TextComponent.fromLegacyText("§7There is a new version available for §3" + name + "§7 (" + latest + "). It is currently running " + currentVersion));
                                }
                            }
                        });
                    } else {
                        sender.sendMessage(TextComponent.fromLegacyText("§cThat is not a valid plugin!"));
                        return;
                    }
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText("§cYou need to specify a plugin!"));
                    return;
                }
            }
        }
        help(sender);
    }

    private void help(CommandSender sender) {
        if (sender.hasPermission("ppm.check")) {
            sender.sendMessage(TextComponent.fromLegacyText("§f+---+ §bParrotPluginManager §f+---+"));
            sender.sendMessage(getTextComponent("§3/bppm check",
                    "§7Checks and updates all loaded plugins by ParrotLync.",
                    "/bppm check"));
            sender.sendMessage(getTextComponent("§3/bppm version <plugin>",
                    "§7Checks which version a plugin is currently running.",
                    "/bppm version "));
        } else {
            sender.sendMessage(TextComponent.fromLegacyText("§cYou don't have permission to do that!"));
        }
    }

    private TextComponent getTextComponent(String message, String hoverMessage, String command) {
        TextComponent main = new TextComponent(message);
        main.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverMessage).create()));
        main.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        return main;
    }
}
