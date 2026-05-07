package me.thinksmp.commands.adminCommands;
import me.thinksmp.functions.Permissions;
import me.thinksmp.managers.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.thinksmp.Core.*;

public class Vanish implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (!player.hasPermission(Permissions.VANISH.getPermission()) || !player.hasPermission(Permissions.ADMIN.getPermission())) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }

        if (getCombat().isInCombat(player)) {
            player.sendMessage("§cYou cannot vanish while in combat.");
            return true;
        }

        if (getRamManager().getVanishedPlayers().contains(player)) {
            VanishManager.unVanish(player);
            getPlayerDataManager().getPlayerData(player).setVanish(false);
            player.sendMessage(ChatColor.GRAY + "You are no longer vanished.");

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer == player) continue;

                if (getRamManager().getVanishedPlayers().contains(onlinePlayer)) {
                    if (player.hasPermission(Permissions.VANISH_SEE.getPermission())) {
                        player.sendMessage(player.getDisplayName() + ChatColor.GRAY + " has left vanish.");
                    }
                }
            }
        } else {
            VanishManager.vanish(player);
            getPlayerDataManager().getPlayerData(player).setVanish(true);
            player.sendMessage(ChatColor.GRAY + "You are now vanished.");
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer == player) continue;

                if (getRamManager().getVanishedPlayers().contains(onlinePlayer)) {
                    if (player.hasPermission(Permissions.VANISH_SEE.getPermission())) {
                        player.sendMessage(player.getDisplayName() + ChatColor.GRAY + " has vanished.");
                    }
                }
            }
        }
        return true;
    }
}
