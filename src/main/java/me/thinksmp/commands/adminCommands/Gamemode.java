package me.thinksmp.commands.adminCommands;

import me.thinksmp.functions.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Gamemode implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        GameMode mode;
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /gm <mode> [player]");
            return true;
        }
        try {
            mode = GameMode.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                int modeNum = Integer.parseInt(args[0]);
                if (modeNum > 3 || modeNum < 0) {
                    sender.sendMessage(ChatColor.RED + "Invalid game mode specified.");
                    return true;
                }
                mode = GameMode.getByValue(modeNum);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                sender.sendMessage(ChatColor.RED + "Invalid game mode specified.");
                return true;
            }
        }
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("This command can only be run by a player or by specifying a player.");
                return true;
            }
            if (!sender.hasPermission(Permissions.ADMIN.getPermission()) || !sender.hasPermission(Permissions.GAMEMODE.getPermission())) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            assert mode != null;
            player.setGameMode(mode);
            sender.sendMessage(ChatColor.GRAY + "Your game mode has been changed to " + ChatColor.GRAY + ChatColor.RED + mode.name().toLowerCase() + ".");
            return true;
        }
        if (!sender.hasPermission(Permissions.ADMIN.getPermission()) || !sender.hasPermission(Permissions.GAMEMODE_OTHERS.getPermission())) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to set other player's game modes.");
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Invalid player specified.");
            return true;
        }
        assert mode != null;
        target.setGameMode(mode);
        sender.sendMessage(ChatColor.GRAY + "Set " + ChatColor.GRAY + target.getDisplayName() + "'s game mode to " + ChatColor.GRAY + ChatColor.RED + mode.name().toLowerCase() + ".");
        return true;
    }
}
