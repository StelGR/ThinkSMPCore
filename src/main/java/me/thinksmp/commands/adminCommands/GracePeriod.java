package me.thinksmp.commands.adminCommands;

import me.thinksmp.functions.Permissions;
import me.thinksmp.managers.PluginTimers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GracePeriod implements CommandExecutor, TabCompleter {

    private final PluginTimers timers;

    public GracePeriod(PluginTimers timers) {
        this.timers = timers;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission(Permissions.ADMIN.getPermission())) {
            sender.sendMessage(color("&cNo permission."));
            return true;
        }

        if (args.length == 0) {
            if (timers.isGracePeriodActive()) {
                timers.stopGracePeriod();
                Bukkit.broadcastMessage(color("&cGrace period has been stopped. PvP is now enabled."));
            } else {
                timers.startGracePeriod();
                Bukkit.broadcastMessage(color("&aGrace period has started for 1 hour. PvP is disabled."));
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            timers.startGracePeriod();
            Bukkit.broadcastMessage(color("&aGrace period has started for 1 hour. PvP is disabled."));
            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            timers.stopGracePeriod();
            Bukkit.broadcastMessage(color("&cGrace period has been stopped. PvP is now enabled."));
            return true;
        }

        if (args[0].equalsIgnoreCase("status")) {
            if (timers.isGracePeriodActive()) {
                sender.sendMessage(color("&aGrace period is active. Time left: &e" + timers.getGracePeriodTimer()));
            } else {
                sender.sendMessage(color("&cGrace period is not active."));
            }

            if (sender instanceof Player player) {
                if (timers.isPVPProtected(player.getUniqueId())) {
                    sender.sendMessage(color("&aYour PvP protection: &e" + timers.getPVPProtectionTimer(player)));
                } else {
                    sender.sendMessage(color("&cYou do not have new-player PvP protection."));
                }
            }

            return true;
        }

        sender.sendMessage(color("&cUsage: /" + label + " <start|stop|status>"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length != 1) {
            return new ArrayList<>();
        }

        List<String> list = Arrays.asList("start", "stop", "status");
        List<String> result = new ArrayList<>();

        for (String entry : list) {
            if (entry.toLowerCase().startsWith(args[0].toLowerCase())) {
                result.add(entry);
            }
        }

        return result;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
